package sg.executor;

import java.util.HashMap;
import java.util.Map;

import sg.constant.Constant;
import sg.mysqldb.DBManager;
import sg.paho.PahoCommunicator;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Executor extends Thread {

	private Map<Integer, String> temp = new HashMap<Integer, String>();
	private Map<Integer, String> hum = new HashMap<Integer, String>();
	private Map<Integer, String> irr = new HashMap<Integer, String>();
	private Map<Integer, String> net = new HashMap<Integer, String>();
	private Map<Integer, String> spo = new HashMap<Integer, String>();
	
	private PahoCommunicator temperature = new PahoCommunicator(temp);
	private PahoCommunicator humidity = new PahoCommunicator(hum);
	private PahoCommunicator irrigatori = new PahoCommunicator(irr);
	private PahoCommunicator reti = new PahoCommunicator(net);
	private PahoCommunicator sportelli = new PahoCommunicator(spo);
	private DBManager dbm = new DBManager();

	public void run() {

		temperature.subscribe("$SYS/broker/log/#", Constant.executor_receiver_id + "_temp");
		
		//humidity.subscribe("openHab/executor/greenhouse/+/actuator/hum", Constant.executor_receiver_id + "_hum");
		//irrigatori.subscribe("openHab/executor/greenhouse/+/actuator/wat", Constant.executor_receiver_id + "_wat");
		//reti.subscribe("openHab/executor/greenhouse/+/actuator/net", Constant.executor_receiver_id + "_net");
		//sportelli.subscribe("openHab/executor/greenhouse/+/actuator/wind", Constant.executor_receiver_id + "_wind");
		
		while (true) {
			try {
				//this.actuatorExecution();
				//this.conditionatorExecution();
				//this.prova(Constant.actuator_channel);
				Thread.sleep(Constant.thread_activation);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// dbm.updateActuatorStatus();
	}

	
	public void prova(String Channel) throws MqttException {
		
		MqttClient mqtt;
		mqtt = new MqttClient("tcp://localhost:1883", Constant.executor_sender_id);
		mqtt.connect();
		
		System.out.println("PROVAAAAAAAA" + mqtt.getTopic(Channel));
		
	}
	
	/*
	 * Condizionatore 
	 * */
	public void conditionatorExecution() {
		
		int resultTemp = 0;
		for (Map.Entry<Integer, String> entry : temp.entrySet()) {
			
			String t = new String();
			t = entry.getValue();
			resultTemp = Integer.parseInt(t);
			
			System.out.println("Valore Temperatura: " + resultTemp );
		}
		dbm.updateActuatorPower(resultTemp, "temp");
		
		temp.clear();
		temperature = new PahoCommunicator(temp);
	}
	
	/*
	 * Deumidificatore 
	 * */
	public void deumidificatorExecution() {
		
		int resultHum = 0;
		for (Map.Entry<Integer, String> entry : hum.entrySet()) {

			String h = new String();
			h = entry.getValue();
			resultHum = Integer.parseInt(h);
		}
		dbm.updateActuatorPower(resultHum, "hum");
		
		hum.clear();
		humidity = new PahoCommunicator(hum);
	}
	
	/*
	 * Irrigatori 
	 * */
	public void sprinklerExecution() {
		
		int resultIrr = 0;
		for (Map.Entry<Integer, String> entry : irr.entrySet()) {

			String i = new String();
			i = entry.getValue();
			resultIrr = Integer.parseInt(i);
		}
		dbm.updateActuatorPower(resultIrr, "wat");
		
		irr.clear();
		irrigatori = new PahoCommunicator(irr);
	}
		
	/*
	 * Sportelli 
	 * */
	public void windowsExecution() {
	
		int resultSport = 0;
		for (Map.Entry<Integer, String> entry : spo.entrySet()) {

			String s = new String();
			s = entry.getValue();
			resultSport = Integer.parseInt(s);
		}
		dbm.updateActuatorPower(resultSport, "wind");
		
		spo.clear();
		sportelli = new PahoCommunicator(spo);
	}
		
	/*
	 * reti 
	 * */
	public void netsExecution() {
		
		int resultReti = 0;
		for (Map.Entry<Integer, String> entry : net.entrySet()) {

			String r = new String();
			r = entry.getValue();
			resultReti = Integer.parseInt(r);
		}
		dbm.updateActuatorPower(resultReti, "net");
		
		net.clear();
		reti = new PahoCommunicator(net);
	}
}
