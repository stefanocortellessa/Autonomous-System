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
    
	private Map<Integer, String> messages = new HashMap<Integer,String>();
    private PahoCommunicator paho = new PahoCommunicator(messages);
    
    private Analyzer analyzer = new Analyzer();
    
    private HashMap<Integer, HashMap<String, Actuator>> actuators = new HashMap<Integer, HashMap<String, Actuator>>();
    private ArrayList<Greenhouse> greenhouses = new ArrayList<Greenhouse>();
    private DBManager dbm = new DBManager();
    
    public void run() {
    	
    	paho.subscribe(Constant.monitor_channel, Constant.monitor_receiver_id);
    	
        while (active) {
				try {
					this.receiveSensorValues();
	    			Thread.sleep(Constant.thread_activation);

				} catch (Exception e) {
					e.printStackTrace();
				}
        }
	}
    
	private void receiveSensorValues(){
		
		/*
		 * to Analyzer = Integer -> id greenhouse, ArrayList -> lista dei sensori
		 * scorro la lista dei messsaggi ricevuti dai canali dei sensori. 
		 * creo la lista toAnalyzer che raccoglier� per ogni serra tutti i dati dei sensori ricevuti
		 * riempio la lista generando la coppia chiave valore (con valore nullo) se la chiave � assente, e 
		 * inserisco tutti i sensori come oggetti Sensor
		 * */
	    Map<Integer,ArrayList<Sensor>> toAnalyzer = new HashMap<Integer,ArrayList<Sensor>>(); 
	    greenhouses = dbm.selectAllGreenhouses();

		for (Greenhouse gh : greenhouses) {

			actuators.put(gh.getId(), dbm.selectGreenhouseActuatorsType(gh.getId()));
		}
		
    	for (Map.Entry<Integer, String> entry : messages.entrySet()){
    		if (entry.getValue() != null) {
    			
    			String message = entry.getValue();
    			Sensor sns = new Sensor(
    					Constant.get_sensor_id_from_message(message),
						Constant.get_sensor_name_from_message(message),
						Constant.get_sensor_type_from_message(message),
						Constant.get_sensor_val_from_message(message),
						Constant.get_gh_id_from_message(message));
    			
				toAnalyzer.putIfAbsent(Constant.get_gh_id_from_message(message),new ArrayList<Sensor>());
				toAnalyzer.get(Constant.get_gh_id_from_message(message)).add(sns);	
				
				if(sns.getType().equals("rain")) {
					if(sns.getValue() == 1) {

						paho.publish("openHab/greenhouse/" + sns.getIdGreenhouse() + "/sensor/"+ sns.getType(),  
								"ON", 
								Constant.monitor_sender_id);
					} else {
						paho.publish("openHab/greenhouse/" + sns.getIdGreenhouse() + "/sensor/"+ sns.getType(),  
								"OFF", 
								Constant.monitor_sender_id);
					}
				}else {
					//vengono pubblicati i valori dei sensori sui diversi topic
					paho.publish("openHab/greenhouse/" + sns.getIdGreenhouse() + "/sensor/"+ sns.getType(),  
							sns.getValue() + "", 
							Constant.monitor_sender_id);
					
					paho.publish("openHab/greenhouse/"+ sns.getIdGreenhouse() + "/actuator/" + sns.getType(),  
							sns.getValue() + "", 
							Constant.monitor_sender_id);
				}
			}
		}
    	
		for (Greenhouse gh : greenhouses) {
			for(Map.Entry<String, Actuator> acts : actuators.get(gh.getId()).entrySet()) {
				
				System.out.println("");
				System.out.println("openHab/greenhouse/"+ acts.getValue().getIdGreenhouse() + "/actuator/" + acts.getValue().getType());
				System.out.println("");
				
				if(acts.getValue().getType().equals("temp") || acts.getValue().getType().equals("hum")) {
					paho.publish("openHab/greenhouse/"+ acts.getValue().getIdGreenhouse() + "/actuator/" + acts.getValue().getType(),  
							acts.getValue().getPower() + "", 
							Constant.monitor_sender_id);
					System.out.println("VALORE TEMP || HUM: " + acts.getValue().getPower());
				} else {
					paho.publish("openHab/greenhouse/"+ acts.getValue().getIdGreenhouse() + "/actuator/" + acts.getValue().getType(),  
							(acts.getValue().getStatus() ? 1 : 0) + "", 
							Constant.monitor_sender_id);
					System.out.println("VALORE :" + (acts.getValue().getStatus()));
				}
			}
		}
		
    	//una volta generata la lista, viene mandata all'analyzer
		analyzer.sensorValuesAnalysis(toAnalyzer);
		
		messages.clear();
		paho = new PahoCommunicator(messages);
	}
}
