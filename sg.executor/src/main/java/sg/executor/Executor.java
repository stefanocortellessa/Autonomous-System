package sg.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sg.constant.Constant;
import sg.paho.PahoCommunicator;

public class Executor{

	private PahoCommunicator paho_executor = new PahoCommunicator();

	public void executor(Map<Integer, ArrayList<String>> actions, Map<Integer, String> newModes) {

		
		
		//per ogni serra, invio le azioni generate dal planner ai relativi attuatori
		for (Map.Entry<Integer, ArrayList<String>> action : actions.entrySet()) {

			
			for (String act : action.getValue()) {
				String type = Constant.parseMessage(act)[0];
				int power = Integer.parseInt(Constant.parseMessage(act)[1]);
														
				this.paho_executor.publish(Constant.executor_channel.replace("+", Integer.toString(action.getKey())).replace("#", type),
						Integer.toString(power));
				
				this.paho_executor.publish(Constant.openhab_actuator_channel.replace("+", Integer.toString(action.getKey())).replace("#", type),
						Integer.toString(power));
			}
			
			
			//eseguo il cambio di modalità della serra generato dal planner, se presente
			if(newModes.containsKey(action.getKey())){
				this.paho_executor.publish(Constant.mode_channel.replace("#", Integer.toString(action.getKey())), 
						newModes.get(action.getKey()));
			}
			
		}
		
		
	}
}
