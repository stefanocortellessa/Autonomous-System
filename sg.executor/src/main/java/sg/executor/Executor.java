package sg.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sg.constant.Constant;
import sg.paho.PahoCommunicator;

public class Executor extends Thread {

	
	private Map<Integer, String> messages = new HashMap<Integer,String>();
    private PahoCommunicator paho = new PahoCommunicator(messages);

	public void executor(HashMap<Integer, ArrayList<String>> actions) {

		for (Map.Entry<Integer, ArrayList<String>> action : actions.entrySet()) {

			if (!(action.getValue().isEmpty())) {
				for (String act : action.getValue()) {

					String type = Constant.parse_message(act)[0];
					int power = Integer.parseInt(Constant.parse_message(act)[1]);
															
															//idGh
					paho.publish("executor/greenhouse/" + action.getKey() + "/actuator/"+ type,  
							power+"", 
							Constant.executor_sender);
					
					//dbm.updateActuatorPower(power, type, action.getKey());
				}
			}
		}
	}
}
