package sg.monitor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import sg.constant.Constant;
import sg.greenhouse.Greenhouse;
import sg.knowledge.Knowledge;
import sg.mysqldb.DBManager;
import sg.paho.PahoCommunicator;
import sg.sensor.Sensor;
import sg.actuator.Actuator;
import sg.analyzer.Analyzer;


public class Monitor extends Thread{

	private ArrayList<Greenhouse> greenhouses = new ArrayList<Greenhouse>();
	private Analyzer analyzer = new Analyzer();
	private DBManager dbm = new DBManager();
	private HashMap<Integer, String> currentModes = new HashMap<Integer, String>();
    private PahoCommunicator paho_sensors = new PahoCommunicator();
    private PahoCommunicator paho_modes = new PahoCommunicator();
    private PahoCommunicator paho_actuators  = new PahoCommunicator();
    private PahoCommunicator paho_signal  = new PahoCommunicator();
    private Calendar clock= Calendar.getInstance();
    private Knowledge knowledge = new Knowledge();
    
    
    public Monitor(){
    	//inizializzo il clock
    	this.clock.set(Calendar.HOUR_OF_DAY, 0);
    	this.clock.set(Calendar.MINUTE, 0);
    	//imposto la modalità "NORMALE" come modalità standard di ogni serra.
    	this.greenhouses = dbm.selectAllGreenhouses();
    	for (Greenhouse gh : this.greenhouses){
    		this.currentModes.put(gh.getId(), Constant.eco_mode);
    	}
    }
    
    
    public void run() {
    	
    	//mi sottoscrivo al canale relativo ai sensori
    	this.paho_sensors.subscribe(Constant.monitor_sensor_channel);
    	//mi sottoscrivo al canale relativo alle modalità delle serre
    	this.paho_modes.subscribe(Constant.mode_channel);
    	//mi sottoscrivo al canale relativo agli attuatori
    	this.paho_actuators.subscribe(Constant.monitor_actuator_channel);
    	//mi sottoscrivo al canale del clock
    	this.paho_signal.subscribe(Constant.clock_channel);

        while (true) {
        
				try {
					/*se è disponibile un messaggio su questo canale, vorrà dire che il ciclo di simulazione è terminato,
					 * e sarà quindi possibile avviare la procedura di Monitor*/
					if (!this.paho_signal.getMessages().isEmpty()){
						this.receiveSensorValues();
					}else{
						Thread.sleep(1000);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
        }
	}
    
	private void receiveSensorValues(){
		
		

		Map<Integer, HashMap<String, Sensor>>  sensors = new HashMap<Integer, HashMap<String, Sensor>>(); 
		
		Map<Integer, HashMap<String, Actuator>> actuators = new HashMap<Integer, HashMap<String, Actuator>>();
		this.paho_signal.getMessages().clear();
		
		for (Map.Entry<Integer, String> mode : this.paho_modes.getMessages().entrySet()) {
    		if(mode.getValue() != null) {
    			
    			this.currentModes.put(Constant.get_id_greenhouse(this.paho_modes.getTopics().get(mode.getKey())),
    			         mode.getValue());
    		}
    	}
	    
		//genero una mappa contenente i sensori di ogni serra
    	for (Map.Entry<Integer, String> entry : this.paho_sensors.getMessages().entrySet()){
    		if (entry.getValue()!=null) {
    			
    			String message = entry.getValue();
    			Sensor sns = new Sensor(
    					Constant.get_element_id_from_message(message),
						Constant.get_element_type_from_message(message),
						Constant.get_element_name_from_message(message),
						Constant.get_sensor_val_from_message(message),
						Constant.get_gh_id_from_message(message));
    			
				sensors.putIfAbsent(Constant.get_gh_id_from_message(message), new HashMap<String, Sensor>());
				sensors.get(Constant.get_gh_id_from_message(message)).put(sns.getType(), sns);
				
				//invio ad openhab i valori dei sensori raccolti
				this.paho_sensors.publish(Constant.openhab_sensor_channel.replace("+", Integer.toString(sns.getIdGreenhouse())).replace("#", sns.getType()),  
							Double.toString(sns.getValue()));
				
				
				
			}
		}
    	
  
    	//genero una mappa contenente gli attuatori di ogni serra
    	for (Map.Entry<Integer, String> entry : this.paho_actuators.getMessages().entrySet()){
    		if (entry.getValue() != null) {
    			
    			String message = entry.getValue();
    			Actuator act = new Actuator(
    					Constant.get_element_id_from_message(message),
    					Constant.get_element_type_from_message(message),
						Constant.get_element_name_from_message(message),
    					Constant.get_actuator_val_from_message(message),
						Constant.get_gh_id_from_message(message));
    			
    		
    			actuators.putIfAbsent(Constant.get_gh_id_from_message(message), new HashMap<String, Actuator>());
				actuators.get(Constant.get_gh_id_from_message(message)).put(act.getType(), act);

			}
		}
    	
    	//elimino i messaggi ricevuti dagli oggetti Paho
    	this.paho_sensors.getMessages().clear();
    	this.paho_actuators.getMessages().clear();
		this.paho_modes.getMessages().clear();
		
		/*controllo se sono stati raccolti correttamente tutti i sensori e gli attuatori. In caso contrario,
		verrà notificato su OpenHab e quella serra non verrà analizzata*/
		Iterator<Map.Entry<Integer, HashMap<String, Sensor>>> s_iterator = sensors.entrySet().iterator();
		
        while (s_iterator.hasNext()) { 
        	boolean cLoss = true;
        	Map.Entry<Integer, HashMap<String, Sensor>> entry = s_iterator.next(); 
           
        	if (entry.getValue().size() != 8) cLoss = false;
        	if (actuators.get(entry.getKey()).size()!= 5)cLoss = false;
        	
        	if(!cLoss){
        		
				this.paho_signal.publish(Constant.alert_channel.replace("#", Integer.toString(entry.getKey())), "1");
				s_iterator.remove();
				actuators.remove(actuators.get(entry.getKey()));
			}else{
				
				this.paho_signal.publish(Constant.alert_channel.replace("#", Integer.toString(entry.getKey())), "0");
			}
        } 
        
		if(!(sensors.isEmpty() || actuators.isEmpty())){
			this.knowledge.insertRecords(sensors, actuators, (Calendar) this.clock.clone());
			this.analyzer.sensorValuesAnalysis(sensors, actuators, this.currentModes, (Calendar) this.clock.clone());
		}
		this.clock.add(Calendar.MINUTE, 30);
		
	}
	
}