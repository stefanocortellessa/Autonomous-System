package sg.updater;

import java.util.Map;

import sg.constant.Constant;
import sg.paho.PahoCommunicator;

public class Updater extends Thread {

	private PahoCommunicator paho_updater = new PahoCommunicator();
		
	public void run() {

		this.paho_updater.subscribe(Constant.updater_channel);
		
		
		while (true) {
			try {
				Thread.sleep(1000);
				this.updateValues();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	public void updateValues(){
	
			//una volta ricevuti i comandi da openHab, li comunico al sistema
			for(Map.Entry<Integer, String> message : this.paho_updater.getMessages().entrySet()) {
				
				int idGh = Constant.get_id_greenhouse(this.paho_updater.getTopics().get(message.getKey()));
				String actuatorType = Constant.get_actuator_type(this.paho_updater.getTopics().get(message.getKey()));	
					
				this.paho_updater.publish(Constant.updater_channel.replace("+", Integer.toString(idGh)).replace("#", actuatorType), 
						message.getValue());
			}
			
			this.paho_updater.getMessages().clear();
			
		}
		
	
}