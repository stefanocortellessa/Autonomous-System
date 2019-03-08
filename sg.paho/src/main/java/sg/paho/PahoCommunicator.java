package sg.paho;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PahoCommunicator implements MqttCallback {
	MqttClient client;
	Map<Integer, String> m;
	private static final AtomicInteger c = new AtomicInteger(0);

	public PahoCommunicator() {
	}

	public PahoCommunicator(Map<Integer, String> m) {
		this.m = m;
	}

	public void connectionLost(Throwable arg0) {
		System.out.println("The connection with the server is lost. !!!!");

	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {
		System.out.println("The delivery has been complete. The delivery token is " + arg0.toString());

	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		//System.out.println("A new message arrived from the topic: \"" + topic + "\". The payload of the message is "
		//		+ message.toString());
		m.putIfAbsent(c.get(), new String());
		m.replace(c.get(), message.toString());
		c.addAndGet(1);
	}

	public void publish(String channel, String mess, String clientId) {
		try {
			MemoryPersistence memoryPersistence = new MemoryPersistence();
			client = new MqttClient("tcp://localhost:1883", clientId, memoryPersistence);
			client.connect();
			MqttMessage message = new MqttMessage();
			message.setPayload(mess.getBytes());
			client.publish(channel, message);
			// client.disconnect();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void subscribe(String c, String id) {

		try {

			client = new MqttClient("tcp://localhost:1883", id);
			client.setCallback(this);
			client.connect();
			client.subscribe(c);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
