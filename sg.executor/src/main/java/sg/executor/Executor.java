package sg.executor;

import java.util.HashMap;
import java.util.Map;

import sg.constant.Constant;
import sg.mysqldb.DBManager;
import sg.paho.PahoCommunicator;

import org.eclipse.paho.client.mqttv3.MqttException;

public class Executor extends Thread {

	private Map<Integer, String> messages = new HashMap<Integer, String>();
	private Map<Integer, String> topics = new HashMap<Integer, String>();
	
	private PahoCommunicator paho = new PahoCommunicator(messages, topics);
	private DBManager dbm = new DBManager();
	
	private int idGh = 0;
	private String actuatorType = "";

	public void run() {

		paho.subscribe("openHab/executor/greenhouse/+/actuator/#", Constant.executor_receiver_id);
		
		while (true) {
			try {
				
				this.updateValues();
				
				Thread.sleep(Constant.thread_activation);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	public void updateValues() throws MqttException {
		
		System.out.println("");
		System.out.println("RETRIEVE VALUES");
		System.out.println("");
		
		if (!messages.isEmpty()) {
			if(!topics.isEmpty()) {				
				
				//System.out.println("MESSAGES: " + messages + ", TOPICS: " + topics );
				for(Map.Entry<Integer, String> tops : topics.entrySet()) {
					
					idGh = Constant.get_id_greenhouse(tops.getValue());
					actuatorType = Constant.get_actuator_type(tops.getValue());
					dbm.updateActuatorPower(messages.get(tops.getKey()), actuatorType, idGh);
				}
				messages.clear();
				topics.clear();
				paho = new PahoCommunicator(messages, topics);
			}
		}
	}
}
