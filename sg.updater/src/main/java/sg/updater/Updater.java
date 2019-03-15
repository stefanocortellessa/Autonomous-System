package sg.updater;

import java.util.HashMap;
import java.util.Map;

import sg.constant.Constant;
import sg.mysqldb.DBManager;
import sg.paho.PahoCommunicator;

import org.eclipse.paho.client.mqttv3.MqttException;

public class Updater extends Thread {

	private Map<Integer, String> values = new HashMap<Integer, String>();
	private Map<Integer, String> topics = new HashMap<Integer, String>();

	private PahoCommunicator paho = new PahoCommunicator(values, topics);
	private DBManager dbm = new DBManager();

	public void run() {

		paho.subscribe(Constant.updater_channel, Constant.updater_receiver);

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

		if (!values.isEmpty()) {
			if (!topics.isEmpty()) {

				for (Map.Entry<Integer, String> tops : topics.entrySet()) {

					int idGh = Constant.get_id_greenhouse(tops.getValue());
					String actuatorType = Constant.get_actuator_type(tops.getValue());

					paho.publish(Constant.updater_channel.replace("+", Integer.toString(idGh)).replace("#", actuatorType),
							values.get(tops.getKey()), Constant.updater_sender);
				}
				values.clear();
				topics.clear();
				paho = new PahoCommunicator(values, topics);
			}
		}
	}
}