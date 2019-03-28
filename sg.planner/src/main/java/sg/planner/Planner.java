package sg.planner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import sg.actuator.Actuator;
import sg.constant.Constant;
import sg.executor.Executor;
import sg.knowledge.Knowledge;
import sg.mysqldb.DBManager;
import sg.sensor.Sensor;
import sg.plan.Plan;
import java.util.Random;

public class Planner {

	private DBManager db = new DBManager();
	private Random rnd = new Random();
	private Executor executor = new Executor();
	private Knowledge knowledge = new Knowledge();


	public void planning(
			Map<Integer, HashMap<String, Integer>> greenhouse_states,
			Map<Integer, HashMap<String, Sensor>> sensors,
			Map<Integer, HashMap<String, Actuator>> actuators, 
			HashMap<Integer, String> currentModes, Calendar clock) {

		
		//inizializzo la mappa che conterrà gli eventuali cambi di modalità di una serra
		Map<Integer, String> newModes = new HashMap<Integer, String>();
		
		//inizializzo la mappa che conterrà le azioni che l'esecutore dovrà comunicare agli attuatori
		Map<Integer, ArrayList<String>> actions = new HashMap<Integer, ArrayList<String>>();

		
		
		for (Map.Entry<Integer, HashMap<String, Integer>> entry : greenhouse_states.entrySet()) {

			
			//creo una mappa che contiene gli stati di temperatura e umidità raccolti nella fase di analisi 
			Map<String, Integer> plan_states = new HashMap<String,Integer>();
			plan_states.put(Constant.int_temp_type, entry.getValue().get(Constant.int_temp_type));
			plan_states.put(Constant.int_hum_type, entry.getValue().get(Constant.int_hum_type));
			actions.putIfAbsent(entry.getKey(), new ArrayList<String>());
			
			//raccolgo dal database i piani attivi
			Map<String,Plan> active_plans = db.selectActivePlans(entry.getKey());
			
			
			for (Map.Entry<String, Integer> state : plan_states.entrySet()) {
				
				/* se esiste una sistuazione di emergenza, la serra passerà allo stato DANGER e verranno generati nuovi piani
				 * o verranno modificati i piani attivi. La serra verrà sigillata nel tentativo di riportarla
				 * nella situazione ottimale il più velocemente possibile senzza contaminazioni di agenti esterni */
				
				if (state.getValue() == Constant.danger_high_int_temp || state.getValue() == Constant.danger_low_int_temp ||
						state.getValue() == Constant.danger_high_int_hum || state.getValue() == Constant.danger_low_int_hum){
				
					actions.get(entry.getKey()).addAll(emergency(entry.getKey(), entry.getValue(), sensors.get(entry.getKey()), 
							actuators.get(entry.getKey()), active_plans, clock));
					newModes.put(entry.getKey(), Constant.danger_mode);
				}
			}
			//se la modalità DANGER è stata impostata, passo alla serra successiva 
			if(newModes.containsKey(entry.getKey())) break;
			
			//controllo se i piani attivi possono essere disattivati
			if (active_plans.size() > 0) {
				for (Plan plan : active_plans.values()) {
					switch (plan.getType()) {
					case (Constant.int_temp_type): {
						
						// Se è stata raggiunta la temperatura ottimale della serra durante un piano, allora lo disattivo
						if (plan_states.get(Constant.int_temp_type) == Constant.opt_int_temp_reach) {
							
							actions.get(entry.getKey())
									.add(Constant.conditioner + Constant.sep + Constant.off);
							db.deactivatePlan(plan.getId(),clock);
							
							//chiudo gli sportelli se attivati dal piano
							if(actuators.get(entry.getKey()).get(Constant.air_vents).getPower() != 0 &&
									entry.getValue().get(Constant.wind_type).equals(Constant.good_wind))
								actions.get(entry.getKey()).add(Constant.air_vents+Constant.sep+Constant.off);
							
							
							if (plan.getMode().equals(Constant.danger_mode)){
								//setto modalità a normale
								newModes.put(entry.getKey(), Constant.eco_mode);
							}
							
						}						
						plan_states.remove(Constant.int_temp_type);
						
						
						break;
					}
					case (Constant.int_hum_type): {
						// Se è stata raggiunta l'umidità ottimale della serra durante un piano, allora lo disattivo
						if (plan_states.get(Constant.int_hum_type) == Constant.opt_int_hum_reach) {
							
							actions.get(entry.getKey())
									.add(Constant.humidifier + Constant.sep + Constant.off);
							
							db.deactivatePlan(plan.getId(),clock);
							
							//chiudo gli sportelli se attivati dal piano
							if(actuators.get(entry.getKey()).get(Constant.air_vents).getPower() != 0 &&
									entry.getValue().get(Constant.wind_type).equals(Constant.good_wind))
								actions.get(entry.getKey()).add(Constant.air_vents+Constant.sep+Constant.off);
							
							if (plan.getMode().equals(Constant.danger_mode)){
								//setto modalità a normale
								newModes.put(entry.getKey(), Constant.eco_mode);
							}
							
						} 
						plan_states.remove(Constant.int_hum_type);
						break;
					}
				}
				}
			}

			//ottengo il set dei possibili valori dei power impostabili per gli attuatori secondo la modalità corrente
			ArrayList<Integer> currentModeRange = Constant.modes.get(currentModes.get(entry.getKey()));
			
		
			// controllo in base agli stati della serra, se è necessario attivare un piano
			if (plan_states.size() > 0) {

				for (Map.Entry<String, Integer> problem : plan_states.entrySet()) {

					// Temperatura
					if (problem.getValue() <= Constant.opt_int_temp_reach) {
						
						if(problem.getValue() == Constant.low_int_temp || problem.getValue() == Constant.high_int_temp){
							//estraggo casualmente il power tra la pool prevista dalla modalità e imposto l'azione necessaria
							actions.get(entry.getKey()).add(this.setConditioner(problem.getValue(), 
									currentModeRange.get(rnd.nextInt(currentModeRange.size()))));
							
							//creo il piano
							db.insertPlan(entry.getKey(), Constant.int_temp_type, currentModes.get(entry.getKey()), true, 
									sensors.get(entry.getKey()).get(Constant.int_temp_type).getValue(), problem.getValue(), clock);
							
						}else if(problem.getValue() == Constant.near_low_int_temp){
						
							
							/*Interrogo la knowledge sull'eventuale possibilità di avviare un piano. 
							 *Se ottengo esito positivo, attivo preventivamente un piano */
							boolean prevision = knowledge.predictiveLowTemperaturePlan(entry.getKey(), clock);
							
							if (prevision){
								actions.get(entry.getKey()).add(Constant.conditioner+Constant.sep+
										Integer.toString(currentModeRange.get(rnd.nextInt(currentModeRange.size()))));
							
								db.insertPlan(entry.getKey(), Constant.int_temp_type, currentModes.get(entry.getKey()), true, 
										sensors.get(entry.getKey()).get(Constant.int_temp_type).getValue(), problem.getValue(), clock);
							}
							
						}else if(problem.getValue() == Constant.near_high_int_temp){
							
							/*Interrogo la knowledge sull'eventuale possibilità di avviare un piano. 
							 *Se ottengo esito positivo, attivo preventivamente un piano */
							boolean prevision = knowledge.predictiveHighTemperaturePlan(entry.getKey(), clock);
							
							if (prevision){
								actions.get(entry.getKey()).add(Constant.conditioner+Constant.neg_sep+
										Integer.toString(currentModeRange.get(rnd.nextInt(currentModeRange.size()))));
							
								db.insertPlan(entry.getKey(), Constant.int_temp_type, currentModes.get(entry.getKey()), true, 
										sensors.get(entry.getKey()).get(Constant.int_temp_type).getValue(), problem.getValue(), clock);
							}
						}
			
						
					// Umidità
					}else if (problem.getValue() <= Constant.opt_int_hum_reach) {
						
						if(problem.getValue() == Constant.low_int_hum || problem.getValue() == Constant.high_int_hum){
							//estraggo casualmente il power tra la pool prevista dalla modalità e imposto l'azione necessaria
							actions.get(entry.getKey()).add(this.setHumidifier(problem.getValue(), 
									currentModeRange.get(rnd.nextInt(currentModeRange.size()))));
							
							//creo il piano
							db.insertPlan(entry.getKey(), Constant.int_hum_type, currentModes.get(entry.getKey()), true, 
									sensors.get(entry.getKey()).get(Constant.int_hum_type).getValue(), problem.getValue(), clock);
							
							
						}else if(problem.getValue() == Constant.near_low_int_hum){
						
							
							/*Interrogo la knowledge sull'eventuale possibilità di avviare un piano. 
							 *Se ottengo esito positivo, attivo preventivamente un piano */
							boolean prevision = knowledge.predictiveLowHumidityPlan(entry.getKey(), clock);
							
							if (prevision) {
								actions.get(entry.getKey()).add(Constant.humidifier+Constant.sep+
										Integer.toString(currentModeRange.get(rnd.nextInt(currentModeRange.size()))));
							
								db.insertPlan(entry.getKey(), Constant.int_hum_type, currentModes.get(entry.getKey()), true, 
									sensors.get(entry.getKey()).get(Constant.int_hum_type).getValue(), problem.getValue(), clock);
							}
							
						}else if(problem.getValue() == Constant.near_high_int_hum){
							
							/*Interrogo la knowledge sull'eventuale possibilità di avviare un piano. 
							 *Se ottengo esito positivo, attivo preventivamente un piano */
							boolean prevision = knowledge.predictiveHighHumidityPlan(entry.getKey(), clock);
							
							if (prevision){
								actions.get(entry.getKey()).add(Constant.humidifier+Constant.neg_sep+
										Integer.toString(currentModeRange.get(rnd.nextInt(currentModeRange.size()))));
							
								db.insertPlan(entry.getKey(), Constant.int_hum_type, currentModes.get(entry.getKey()), true, 
										sensors.get(entry.getKey()).get(Constant.int_hum_type).getValue(), problem.getValue(), clock);
								}
							
						}
					} 
				}
			}
			
			//genero le reazioni in base agli eventi esterni
			
			//se la modalità corrente è DANGER, le reazioni non verranno generate 
			if (!(currentModes.get(entry.getKey()).equals(Constant.danger_mode))){
				//booleano che fornisce priorità sullo sfruttamento degli sportelli
				boolean priority = false;
	
				//Temperatura
				if (entry.getValue().get(Constant.int_temp_type) == Constant.low_int_temp ||
						entry.getValue().get(Constant.int_temp_type) == Constant.near_low_int_temp) {
	
					// se la temperatura esterna è minore della temperatura interna e se gli sportelli sono aperti, li chiudo
					if (sensors.get(entry.getKey()).get(Constant.ext_temp_type).getValue() < sensors.get(entry.getKey()).get(Constant.int_temp_type).getValue() ) {
						if (actuators.get(entry.getKey()).get(Constant.air_vents).getPower()!=0) {
							actions.get(entry.getKey()).add(Constant.air_vents + Constant.sep + Constant.off);
							
							priority = true;
						}
					} 
					
					/* se la temperatura esterna è maggiore della temperatura interna (tenendo conto di un fattore di incidenza pari a 3), 
					 * se gli sportelli sono chiusi e se il vento non è pericoloso, li apro */
					else if (sensors.get(entry.getKey()).get(Constant.ext_temp_type).getValue() > sensors.get(entry.getKey()).get(Constant.int_temp_type).getValue() +3){
						if (!(entry.getValue().get(Constant.wind_type) == Constant.wind_danger) && 
								actuators.get(entry.getKey()).get(Constant.air_vents).getPower()==0) {
							actions.get(entry.getKey()).add(Constant.air_vents + Constant.sep + Constant.on);
							
							priority = true;
						}
					}
	
					// se la luce è moderata, disattivo le reti
					if (entry.getValue().get(Constant.light_type) == Constant.good_light) {
							actions.get(entry.getKey()).add(Constant.net + Constant.sep + Constant.off);
					}
					
				} else if (entry.getValue().get(Constant.int_temp_type) == Constant.high_int_temp ||
						entry.getValue().get(Constant.int_temp_type) == Constant.near_high_int_temp) {
					
					// se la temperatura esterna è maggiore della temperatura interna e se gli sportelli sono aperti, li chiudo
					if (sensors.get(entry.getKey()).get(Constant.ext_temp_type).getValue() > sensors.get(entry.getKey()).get(Constant.int_temp_type).getValue()) {
						if (actuators.get(entry.getKey()).get(Constant.air_vents).getPower() != 0) {
							actions.get(entry.getKey()).add(Constant.air_vents + Constant.sep + Constant.off);
							
							priority = true;
						}
					} 
					
					/* se la temperatura esterna è minore della temperatura interna (tenendo conto di un fattore di incidenza pari a -3), 
					 * se gli sportelli sono chiusi e se il vento non è pericoloso, li apro */
					else if (sensors.get(entry.getKey()).get(Constant.ext_temp_type).getValue() < sensors.get(entry.getKey()).get(Constant.int_temp_type).getValue() - 3){
						if (!(entry.getValue().get(Constant.wind_type) == Constant.wind_danger) && 
								actuators.get(entry.getKey()).get(Constant.air_vents).getPower() == 0) {
							actions.get(entry.getKey()).add(Constant.air_vents + Constant.sep + Constant.on);
							
							priority = true;
						}
					}
	
					// se luce è moderata, attivo le reti
					if (entry.getValue().get(Constant.light_type) == Constant.good_light) {
							actions.get(entry.getKey()).add(Constant.net + Constant.sep + Constant.on);
					}
				}
				
				
				
			//Umidità
				
			//se non è già stata prevista una reazione sugli sportelli nella sezione Temperatura, allora accedo alla sezione Umidità
				if (!priority){
					if (entry.getValue().get(Constant.int_hum_type) == Constant.low_int_hum) {
	
					// se l'umidità esterna è minore dell'umidità interna e se gli sportelli sono aperti, li chiudo
						if (sensors.get(entry.getKey()).get(Constant.ext_hum_type).getValue() < sensors.get(entry.getKey()).get(Constant.int_hum_type).getValue()) {
							if (actuators.get(entry.getKey()).get(Constant.air_vents).getPower() != 0) {
								actions.get(entry.getKey()).add(Constant.air_vents + Constant.sep + Constant.off);
							}
						} 
					
					/* se l'umidità esterna è maggiore dell'umidità interna (tenendo conto di un fattore di incidenza pari a 3), 
					 * se gli sportelli sono chiusi e se il vento non è pericoloso, li apro */
					
						else if (sensors.get(entry.getKey()).get(Constant.ext_hum_type).getValue() > sensors.get(entry.getKey()).get(Constant.int_hum_type).getValue() + 3) {
							if (!(entry.getValue().get(Constant.wind_type) == Constant.wind_danger) && 
									actuators.get(entry.getKey()).get(Constant.air_vents).getPower() == 0) {
								actions.get(entry.getKey()).add(Constant.air_vents + Constant.sep + Constant.on);
							}
						}
	
					} else if (entry.getValue().get(Constant.int_hum_type) == Constant.high_int_hum) {
	
						// se l'umidità esterna è maggiore dell'umidità interna e se gli sportelli sono aperti, li chiudo
						if (sensors.get(entry.getKey()).get(Constant.ext_hum_type).getValue() > sensors.get(entry.getKey()).get(Constant.int_hum_type).getValue()) {
							if (actuators.get(entry.getKey()).get(Constant.air_vents).getPower() != 0) {
								actions.get(entry.getKey()).add(Constant.air_vents + Constant.sep + Constant.off);
							}
						} 
					
						/* se l'umidità esterna è minore dell'umidità interna (tenendo conto di un fattore di incidenza pari a -3), 
						 * se gli sportelli sono chiusi	e se il vento non è pericoloso, li apro */
						else if (sensors.get(entry.getKey()).get(Constant.ext_hum_type).getValue() < sensors.get(entry.getKey()).get(Constant.int_hum_type).getValue() - 3) {
							if (!(entry.getValue().get(Constant.wind_type) == Constant.wind_danger) && 
									actuators.get(entry.getKey()).get(Constant.air_vents).getPower() == 0) {
								actions.get(entry.getKey()).add(Constant.air_vents + Constant.sep + Constant.on);
							}
						}
					}
				}
			
			
			
				//vento 
		
				//se il vento è periocolo e gli sportelli sono aperti, li chiudo
				if(entry.getValue().get(Constant.wind_type) == Constant.wind_danger){
					if(actuators.get(entry.getKey()).get(Constant.air_vents).getPower() != 0){
						actions.get(entry.getKey()).add(Constant.air_vents+Constant.sep+Constant.off);
					}
				}	
				
				//luce
				
				//se la luce è pericolosa e le reti sono disattivate, le attivo 
				if(entry.getValue().get(Constant.light_type) == Constant.light_danger){
					if(actuators.get(entry.getKey()).get(Constant.net).getPower() == 0){
						actions.get(entry.getKey()).add(Constant.net+Constant.sep+Constant.on);
					}
				}
				
				/*se non vengono previste reazioni relative alle reti ombreggianti oppure la luce è assente, 
				e se le reti sono attivate, le disattivo */
				else if(!(actions.get(entry.getKey()).contains(Constant.net + Constant.sep + Constant.on) || 
						actions.get(entry.getKey()).contains(Constant.net + Constant.sep + Constant.off)) ||
						entry.getValue().get(Constant.light_type) == Constant.no_light){
					if(actuators.get(entry.getKey()).get(Constant.net).getPower() != 0){
						actions.get(entry.getKey()).add(Constant.net + Constant.sep + Constant.off);
					
					}
					
				}
			
			}
			
			
			//Umidità del terreno 
			
			//se viene rilevata un'umidità del terreno insufficiente e gli irrigatori sono spenti, li accendo
			if (entry.getValue().get(Constant.terrain_hum_type) == Constant.low_int_terr_hum){
				if(actuators.get(entry.getKey()).get(Constant.sprinkler).getPower() == 0){
					actions.get(entry.getKey()).add(Constant.sprinkler+Constant.sep+Constant.on);
				}	
			}
			
			//se viene rilevata un'umidità del terreno ottimale e gli irrigatori sono accesi, li spengo
			else if (entry.getValue().get(Constant.terrain_hum_type) == Constant.opt_int_terr_hum_reach){
				if(actuators.get(entry.getKey()).get(Constant.sprinkler).getPower() != 0){
					actions.get(entry.getKey()).add(Constant.sprinkler+Constant.sep+Constant.off);
				}
			}	
			
			
		}
		executor.executor(actions,newModes);
	}
	
	public ArrayList<String> emergency(int idGh,Map<String,Integer> states, HashMap<String,Sensor> sensors, 
			HashMap<String,Actuator> actuators,Map<String, Plan> active_plans, Calendar datetime){
		
		ArrayList<String> reactions = new ArrayList<String>();
		
		
		if(active_plans.containsKey(Constant.int_temp_type) && !(active_plans.get(Constant.int_temp_type).getMode().equals(Constant.danger_mode))) {
			//converto il piano temperatura in corso a DANGER
			db.toDanger(active_plans.get(Constant.int_temp_type).getId());
		
			if (states.get(Constant.int_temp_type) == Constant.low_int_temp || 
					states.get(Constant.int_temp_type) == Constant.danger_low_int_temp){
				
				
				reactions.add(Constant.conditioner+Constant.sep+Constant.modes.get(Constant.danger_mode).get(0));
						
			}else if(states.get(Constant.int_temp_type) == Constant.high_int_temp || 
					states.get(Constant.int_temp_type) == Constant.danger_high_int_temp){
					
				
						
				reactions.add(Constant.conditioner+Constant.neg_sep+Constant.modes.get(Constant.danger_mode).get(0));			
			}	
		}
		if(!(active_plans.containsKey(Constant.int_temp_type))) { 
				
			if (states.get(Constant.int_temp_type) == Constant.low_int_temp || 
					states.get(Constant.int_temp_type) == Constant.danger_low_int_temp){
				//creo il piano DANGER
				db.insertPlan(idGh, Constant.int_temp_type, Constant.danger_mode, true, 
						sensors.get(Constant.int_temp_type).getValue(), states.get(Constant.int_temp_type), datetime);
				
				
				reactions.add(Constant.conditioner+Constant.sep+Constant.modes.get(Constant.danger_mode).get(0));
						
			}else if(states.get(Constant.int_temp_type) == Constant.high_int_temp || 
					states.get(Constant.int_temp_type) == Constant.danger_high_int_temp){
						
				//creo il piano DANGER
				db.insertPlan(idGh, Constant.int_temp_type, Constant.danger_mode, true, 
						sensors.get(Constant.int_temp_type).getValue(), states.get(Constant.int_temp_type), datetime);		
				
				reactions.add(Constant.conditioner+Constant.neg_sep+Constant.modes.get(Constant.danger_mode).get(0));
						
			}				
		}
		
		
		if(active_plans.containsKey(Constant.int_hum_type) && !(active_plans.get(Constant.int_hum_type).getMode().equals(Constant.danger_mode))) {
		
			//converto il piano temperatura in corso a DANGER
			db.toDanger(active_plans.get(Constant.int_temp_type).getId());
		
			if (states.get(Constant.int_hum_type) == Constant.low_int_hum || 
					states.get(Constant.int_hum_type) == Constant.danger_low_int_hum){
			
				reactions.add(Constant.humidifier+Constant.sep+Constant.modes.get(Constant.danger_mode).get(0));
				
			}else if(states.get(Constant.int_hum_type) == Constant.high_int_hum || 
					states.get(Constant.int_hum_type) == Constant.danger_high_int_hum){
		
				reactions.add(Constant.humidifier+Constant.neg_sep+Constant.modes.get(Constant.danger_mode).get(0));
				
			
			}
		}
		
		if(!(active_plans.containsKey(Constant.int_temp_type))) { 
			
			if (states.get(Constant.int_hum_type) == Constant.low_int_hum || 
					states.get(Constant.int_hum_type) == Constant.danger_low_int_hum){
				
				//creo il piano DANGER
				db.insertPlan(idGh, Constant.int_hum_type, Constant.danger_mode, true, 
						sensors.get(Constant.int_hum_type).getValue(), states.get(Constant.int_hum_type), datetime);		
				
				reactions.add(Constant.humidifier+Constant.sep+Constant.modes.get(Constant.danger_mode).get(0));
				
			}else if(states.get(Constant.int_hum_type) == Constant.high_int_hum || 
					states.get(Constant.int_hum_type) == Constant.danger_high_int_hum){

				//creo il piano DANGER
				db.insertPlan(idGh, Constant.int_hum_type, Constant.danger_mode, true, 
						sensors.get(Constant.int_hum_type).getValue(), states.get(Constant.int_hum_type), datetime);
				
				reactions.add(Constant.humidifier+Constant.neg_sep+Constant.modes.get(Constant.danger_mode).get(0));
				
			
			}
		}
		
		
		// eseguo reazioni speciali, in modo da salvaguardare la serra
		
		//Vento 

		//Isolo la serra da temperatura ed umidità esterna
		if(actuators.get(Constant.air_vents).getPower() != 0){
			reactions.add(Constant.air_vents+Constant.sep+Constant.off);
		}
		
		//luce
		
		//Isolo la serra dall'intensità luminosa esterna
		if(actuators.get(Constant.net).getPower() == 0){
			reactions.add(Constant.net+Constant.sep+Constant.on);
		}
		
		//Umidità del terreno

		
		//se viene rilevata un'umidità del terreno insufficiente e gli irrigatori sono spenti, li accendo
		if (states.get(Constant.terrain_hum_type) == Constant.low_int_terr_hum){
			if(actuators.get(Constant.sprinkler).getPower() == 0){
				reactions.add(Constant.sprinkler+Constant.sep+Constant.on);
			}	
		}
		
		//se viene rilevata un'umidità del terreno ottimale e gli irrigatori sono accesi, li spengo
		else if (states.get(Constant.terrain_hum_type) == Constant.opt_int_terr_hum_reach){
			if(actuators.get(Constant.sprinkler).getPower() != 0){
				reactions.add(Constant.sprinkler+Constant.sep+Constant.off);
			}
		}			
		
		
		return reactions;
	}
	


		

	//Temperatura
	public String setConditioner(Integer state,int power) {

		String cond_action = new String();
		//imposto il condizionatore al valore di potenza selezionato
		if (state == Constant.low_int_temp) {
			cond_action = Constant.conditioner+Constant.sep+Integer.toString(power);
			
		} else if ( state == Constant.high_int_temp) {	
			cond_action = Constant.conditioner+Constant.neg_sep+Integer.toString(power);
		}
		return cond_action;
	}
	
	//Umidità
	public String setHumidifier(Integer state,int power) {
		
		String hum_action = new String();	
		//imposto l'umidificatore al valore di potenza selezionato
		if (state == Constant.low_int_hum) {
			hum_action = Constant.humidifier+Constant.sep+Integer.toString(power);
		
		} else if (state == Constant.high_int_hum) {
			hum_action = Constant.humidifier+Constant.neg_sep+Integer.toString(power);
		}
		return hum_action;
	}
	
}							
