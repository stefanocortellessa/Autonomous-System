package sg.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sg.constant.Constant;
import sg.greenhouse.Greenhouse;
import sg.mysqldb.DBManager;
import sg.paho.PahoCommunicator;
import sg.sensor.Sensor;
import sg.actuator.Actuator;
import sg.analyzer.*;


public class Monitor extends Thread{
	
	private volatile boolean active = true;
    
	private Analyzer analyzer = new Analyzer();
	private HashMap<Integer, String> currentMode = new HashMap<Integer, String>();
	private HashMap<Integer, String> topics = new HashMap<Integer, String>();
	private HashMap<Integer, String> messages = new HashMap<Integer,String>();
	private HashMap<Integer, String> modes = new HashMap<Integer,String>();
    private PahoCommunicator paho = new PahoCommunicator(messages);
    private PahoCommunicator mode = new PahoCommunicator(modes, topics);
    private int idGh = 0;
    
    private HashMap<Integer, HashMap<String, Actuator>> actuators = new HashMap<Integer, HashMap<String, Actuator>>();
    private ArrayList<Greenhouse> greenhouses = new ArrayList<Greenhouse>();
    private DBManager dbm = new DBManager();
    
    public void run() {
    	
    	paho.subscribe(Constant.monitor_channel, Constant.monitor_receiver);
    	mode.subscribe(Constant.mode_channel, Constant.mode_receiver);
    	
    	
    	for (Map.Entry<Integer, String> mode : modes.entrySet()) {
    		if(mode.getValue() != null) {
    			
    			currentMode.put(new Integer(topics.get(mode.getKey())), mode.getValue());
    		}
    	}
    	
        while (active) {
				try {
					this.receiveSensorValues(currentMode);
	    			Thread.sleep(Constant.thread_activation);

				} catch (Exception e) {
					e.printStackTrace();
				}
        }
	}
    
	private void receiveSensorValues(HashMap<Integer, String> currentMode){
		
		/*
		 * to Analyzer = Integer -> id greenhouse, ArrayList -> lista dei sensori
		 * scorro la lista dei messsaggi ricevuti dai canali dei sensori. 
		 * creo la lista toAnalyzer che raccoglier� per ogni serra tutti i dati dei sensori ricevuti
		 * riempio la lista generando la coppia chiave valore (con valore nullo) se la chiave � assente, e 
		 * inserisco tutti i sensori come oggetti Sensor
		 * */
		Map<Integer, HashMap<String, Sensor>>  toAnalyzer = new HashMap<Integer, HashMap<String, Sensor>> (); 
	    greenhouses = dbm.selectAllGreenhouses();
		
    	for (Map.Entry<Integer, String> entry : messages.entrySet()){
    		if (entry.getValue() != null) {
    			
    			String message = entry.getValue();
    			Sensor sns = new Sensor(
    					Constant.get_sensor_id_from_message(message),
						Constant.get_sensor_name_from_message(message),
						Constant.get_sensor_type_from_message(message),
						Constant.get_sensor_val_from_message(message),
						Constant.get_gh_id_from_message(message));
    			
				toAnalyzer.putIfAbsent(Constant.get_gh_id_from_message(message), new HashMap<String, Sensor>());
				toAnalyzer.get(Constant.get_gh_id_from_message(message)).put(sns.getType(), sns);	
				
				if(sns.getType().equals("rain")) {
					if(sns.getValue() == 1) {

						paho.publish("openHab/greenhouse/" + sns.getIdGreenhouse() + "/sensor/"+ sns.getType(),  
								"ON", 
								Constant.monitor_sender);
					} else {
						paho.publish("openHab/greenhouse/" + sns.getIdGreenhouse() + "/sensor/"+ sns.getType(),  
								"OFF", 
								Constant.monitor_sender);
					}
				}else {
					//vengono pubblicati i valori dei sensori sui diversi topic
					paho.publish("openHab/greenhouse/" + sns.getIdGreenhouse() + "/sensor/"+ sns.getType(),  
							sns.getValue() + "", 
							Constant.monitor_sender);
					
					paho.publish("openHab/greenhouse/"+ sns.getIdGreenhouse() + "/actuator/" + sns.getType(),  
							sns.getValue() + "", 
							Constant.monitor_sender);
				}
			}
		}
    	
    	
		for (Greenhouse gh : greenhouses) {
		
			actuators.put(gh.getId(), dbm.selectGreenhouseActuatorsType(gh.getId()));
			
			for(Map.Entry<String, Actuator> acts : actuators.get(gh.getId()).entrySet()) {
				
				System.out.println("");
				System.out.println("openHab/greenhouse/"+ acts.getValue().getIdGreenhouse() + "/actuator/" + acts.getValue().getType());
				System.out.println("");
				
				if(acts.getValue().getType().equals("temp") || acts.getValue().getType().equals("hum")) {
					paho.publish("openHab/greenhouse/"+ acts.getValue().getIdGreenhouse() + "/actuator/" + acts.getValue().getType(),  
							acts.getValue().getPower() + "", 
							Constant.monitor_sender);
					System.out.println("VALORE TEMP || HUM: " + acts.getValue().getPower());
				} else {
					paho.publish("openHab/greenhouse/"+ acts.getValue().getIdGreenhouse() + "/actuator/" + acts.getValue().getType(),  
							(acts.getValue().getStatus() ? 1 : 0) + "", 
							Constant.monitor_sender);
					System.out.println("VALORE :" + (acts.getValue().getStatus()));
				}
			}
		}
		
    	//una volta generata la lista, viene mandata all'analyzer
		analyzer.sensorValuesAnalysis(toAnalyzer, actuators, currentMode);
		
		messages.clear();
		paho = new PahoCommunicator(messages);
	}
}