package sg.paho;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PahoCommunicator implements MqttCallback {

	int k = 0;
	MqttClient client;
	Map<Integer, String> messages = new ConcurrentHashMap<Integer,String>();
	Map<Integer, String> topics = new ConcurrentHashMap<Integer,String>();

	public PahoCommunicator() {}

	public Map<Integer, String> getMessages() {
		return messages;
	}

	public void setMessages(Map<Integer, String> messages) {
		this.messages = messages;
	}

	public Map<Integer, String> getTopics() {
		return topics;
	}

	public void setTopics(Map<Integer, String> topics) {
		this.topics = topics;
	}

	public void connectionLost(Throwable arg0) {

		System.out.println("The connection with the server is lost !!!!");
	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {

		System.out.println("The delivery has been complete. The delivery token is ");
	}

	public void messageArrived(String topic, MqttMessage message) throws Exception {

		this.messages.put(this.k,message.toString());
		this.topics.put(this.k , topic);
		/*utilizzo il contatore per identificare univocamente il topic da cui deriva ogni messaggio.
		Non avendo questo valore altra utilità, uso l'operazione di modulo per evitare che assuma valori troppo
		elevati nel tempo, fissando il valore 1000 come secondo operatore dell'operazione*/
		this.k %= 1000;
		this.k++;
	}
	
	public void clear(){
		this.messages.clear();
		this.topics.clear();
	}

	public void publish(String channel, String mess) {

		try {
			MemoryPersistence memoryPersistence = new MemoryPersistence();
			client = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId(), memoryPersistence);
			client.connect();
			MqttMessage message = new MqttMessage();
			message.setPayload(mess.getBytes());
			message.setQos(2);
			client.publish(channel, message);
			client.disconnect();
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void subscribe(String channel) {

		try {
			MemoryPersistence memoryPersistence = new MemoryPersistence();
			client = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId(), memoryPersistence);
			client.setCallback(this);
			
			client.connect();
			client.subscribe(channel);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
}
