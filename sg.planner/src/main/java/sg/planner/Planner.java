package sg.planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sg.actuator.Actuator;
import sg.constant.Constant;
import sg.mysqldb.DBManager;
import sg.sensor.Sensor;
import sg.plan.Plan;
import java.util.Random;

public class Planner {

	private DBManager db = new DBManager();
	private Random rnd = new Random();

	// Controllo se ci sono possibili azioni reattive per l'umidità
	public ArrayList<String> checkReactiveTemperatureUpdates(
			Integer problem, 
			HashMap<String, Integer> states,
			HashMap<String, Sensor> sensors, 
			HashMap<String, Actuator> actuators) {

		ArrayList<String> reactions = new ArrayList<String>();
		//System.out.println("REAZIONE Temperatura: " + reactions);

		if (problem == Constant.low_int_temp) {
			//System.out.println("Temperatura Bassa");

			// se temperatura esterna < temperatura interna
			if (sensors.get(Constant.ext_temp_type).getValue() <= sensors.get(Constant.int_temp_type).getValue()) {

				//System.out.println("Esterna <= Interna ---- Temperatura");
				// se attuatori sportelli attivi
				if (actuators.get(Constant.air_vents).getStatus()) {
					// chiudo
					reactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);
				}
			} else {

				//System.out.println("Esterna > Interna ---- Temperatura");
				// se attuatori sportelli chiusi
				if (!actuators.get(Constant.air_vents).getStatus()) {
					// apro
					reactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_on);
				}
			}

			// se luce è buona
			if (states.get(Constant.light_type) == Constant.good_light) {
				//System.out.println("Luce Buona");
				// se attuatori reti attive
				if (actuators.get(Constant.net).getStatus()) {
					// chiudo
					//System.out.println("Attuatori attivi, li disattivo");
					reactions.add(Constant.net + Constant.positive_separator + Constant.planner_off);
				}
			}
		} else if (problem == Constant.high_int_temp) {
			//System.out.println("Temperatura Alta");

			// se temperatura esterna > temperatura interna
			if (sensors.get(Constant.ext_temp_type).getValue() > sensors.get(Constant.int_temp_type).getValue()) {
				// se attuatori sportelli attivi
				//System.out.println("Esterna > Interna");
				if (actuators.get(Constant.air_vents).getStatus()) {
					//System.out.println("Attuatori attivi, li disattivo");
					// chiudo
					reactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);
				}
			} else {
				//System.out.println("Esterna <= Interna");
				// se attuatori sportelli chiusi
				if (!actuators.get(Constant.air_vents).getStatus()) {
					//System.out.println("Attuatori Chiusi, li apro");
					// apro
					reactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_on);
				}
			}

			// se luce è buona
			if (states.get(Constant.light_type) == Constant.good_light) {
				//System.out.println("Luce Buona");
				// se attuatori reti disattivate
				if (!actuators.get(Constant.net).getStatus()) {
					//System.out.println("Reti disattivate, le attivo");
					// apro
					reactions.add(Constant.net + Constant.positive_separator + Constant.planner_on);
				}
			}
		}
		System.out.println("***********************");
		System.out.println("REAZIONI TEMPERATURA: " + reactions);
		System.out.println("***********************");
		return reactions;
	}

	// Controllo se ci sono possibili azioni reattive per l'umidità
	public ArrayList<String> checkReactiveHumidityUpdates(
			Integer problem, 
			HashMap<String, Integer> states,
			HashMap<String, Sensor> sensors, 
			HashMap<String, Actuator> actuators) {

		ArrayList<String> reactions = new ArrayList<String>();
		//System.out.println("REAZIONE Umidità: " + reactions);

		if (problem == Constant.low_int_hum) {
			//System.out.println("Umidità Bassa");

			// se umidità esterna < umidità interna
			if (sensors.get(Constant.ext_hum_type).getValue() <= sensors.get(Constant.int_hum_type).getValue()) {
				//System.out.println("Esterna <= Interna ---- Umidità");
				// se attuatori sportelli attivi
				if (actuators.get(Constant.air_vents).getStatus()) {
					// chiudo
					reactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);
				}
			} else {
				//System.out.println("Esterna > Interna ---- Umidità");
				// se attuatori sportelli chiusi
				if (!actuators.get(Constant.air_vents).getStatus()) {
					// apro
					reactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_on);
				}
			}

		} else if (problem == Constant.high_int_hum) {
			//System.out.println("Umidità Alta");
			// se temperatura esterna > temperatura interna
			if (sensors.get(Constant.ext_temp_type).getValue() > sensors.get(Constant.int_temp_type).getValue()) {
				//System.out.println("Esterna > Interna ---- Umidità");
				// se attuatori sportelli attivi
				if (actuators.get(Constant.air_vents).getStatus()) {
					// chiudo
					reactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);
				}
			} else {
				//System.out.println("Esterna <= Interna ---- Umidità");
				// se attuatori sportelli chiusi
				if (!actuators.get(Constant.air_vents).getStatus()) {
					// apro
					reactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_on);
				}
			}
		}
		//System.out.println("***********************");
		//System.out.println("REAZIONI UMIDITà: " + reactions);
		//System.out.println("***********************");
		return reactions;
	}

	public ArrayList<String> activeModality(		
						Integer problem, 
						HashMap<String, Integer> states,
						HashMap<String, Sensor> sensors, 
						HashMap<String, Actuator> actuators,
						int power) {

		ArrayList<String> ecoReactions = new ArrayList<String>();
		System.out.println("ECO Modality Activated!");
		
		//UMIDITà
		//sto sotto la soglia
		if (problem == Constant.low_int_hum) {
			//System.out.println("Temperatura Bassa");
	
			// se umidità esterna > umidità interna
			if (sensors.get(Constant.ext_hum_type).getValue() > sensors.get(Constant.int_hum_type).getValue()) {
				
				if (!actuators.get(Constant.air_vents).getStatus()) {
					
					ecoReactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_on);
				}else {
					//accendo il deumificatore a 1
					ecoReactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);
					ecoReactions.add(Constant.humidifier + Constant.positive_separator + (power));
				}
			} 
			//supero la soglia
		} else if (problem == Constant.high_int_hum) {
	
			// se umidità esterna <= umidità interna
			if (sensors.get(Constant.ext_hum_type).getValue() <= sensors.get(Constant.int_hum_type).getValue()) {

				//System.out.println("Esterna > Interna");
				if (!actuators.get(Constant.air_vents).getStatus()) {

					// attivo
					ecoReactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_on);
				} else {
					//accendo il deumificatore a -1
					ecoReactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);
					ecoReactions.add(Constant.humidifier + Constant.positive_separator + (-power));
				}
			} 
		//TEMPERATURA
		//sotto la soglia
		} else if (problem == Constant.low_int_temp) {
			//System.out.println("Temperatura Bassa");
	
			// se temperatura esterna > temperatura interna
			if (sensors.get(Constant.ext_temp_type).getValue() > sensors.get(Constant.int_temp_type).getValue()) {
				
				if (!actuators.get(Constant.air_vents).getStatus()) {
					
					ecoReactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_on);
				} else {
					//accendo il condizionatore a 1
					ecoReactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);
					ecoReactions.add(Constant.conditioner + Constant.positive_separator + (power));
				}
			} 
		//supero la soglia
		} else if (problem == Constant.high_int_temp) {
	
			// se temperatura esterna > temperatura interna
			if (sensors.get(Constant.ext_temp_type).getValue() <= sensors.get(Constant.int_temp_type).getValue()) {

				//System.out.println("Esterna > Interna");
				if (!actuators.get(Constant.air_vents).getStatus()) {

					// attivo
					ecoReactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_on);
				}else {
					//System.out.println("Esterna > Interna");
					//accendo il deumificatore a -1
					ecoReactions.add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);
					ecoReactions.add(Constant.conditioner + Constant.positive_separator + (-power));
				}
			} 
		}
		//System.out.println("***********************");
		//System.out.println("REAZIONI ECO MODE: " + ecoReactions);
		//System.out.println("***********************");
		return ecoReactions;
	}

	public void normalModality() {

		System.out.println("NORMAL Modality Activated!");
	}

	public void optimalModality() {

		System.out.println("OPTIMAL Modality Activated!");
	}

	public void dangerModality() {

		System.out.println("DANGER Modality Activated!");
	}

	public void planning(
			Map<Integer, HashMap<String, Integer>> greenhouse_states,
			Map<Integer, HashMap<String, Sensor>> sensors_per_greenhouse,
			Map<Integer, HashMap<String, Actuator>> actuators, 
			HashMap<Integer, String> currentModes) {

		Map<Integer, ArrayList<String>> actions = new HashMap<Integer, ArrayList<String>>();

		for (Map.Entry<Integer, HashMap<String, Integer>> entry : greenhouse_states.entrySet()) {

			HashMap<String, Integer> gh_states = entry.getValue();
			actions.putIfAbsent(entry.getKey(), new ArrayList<String>());
			ArrayList<Plan> active_plans = db.selectActivePlans(entry.getKey());

			/*
			if (active_plans.size() > 0) {
				for (Plan plan : active_plans) {

					switch (plan.getType()) {

					case (Constant.int_temp_type): {

						// Se piano ottimale ha raggiunto una buona temperatura interna
						if (gh_states.get(Constant.int_temp_type) == Constant.opt_int_temp_reach) {

							// disattivo il piano
							actions.get(entry.getKey())
									.add(Constant.conditioner + Constant.positive_separator + Constant.planner_off);
							System.out.println("Optimal Temperature Reached! Deactivating Plan");
							db.deactivatePlan(plan.getId());
						} else {

							// db.updateCurrentVPlan(plan.getId(),
							// sensors_per_greenhouse.get(entry.getKey()).get(Constant.int_temp_type).getValue());
							System.out.println("Still working for temperature..");
						}

						this.checkReactiveTemperatureUpdates(gh_states.get(plan.getType()), entry.getValue(),
								sensors_per_greenhouse.get(entry.getKey()), actuators.get(entry.getKey()));

						gh_states.remove(Constant.int_temp_type);
						break;
					}
					case (Constant.int_hum_type): {

						if (gh_states.get(Constant.opt_int_hum_reach) != null) {
							actions.get(entry.getKey())
									.add(Constant.humidifier + Constant.positive_separator + Constant.planner_off);
							System.out.println("Optimal Humidity Reached! Deactivating Plan");
							db.deactivatePlan(plan.getId());
						} else {
							db.updateCurrentVPlan(plan.getId(),
									sensors_per_greenhouse.get(entry.getKey()).get(Constant.int_hum_type).getValue());
							System.out.println("Still working for humidity...");
						}

						// this.checkReactiveHumidityUpdates(gh_states.get(plan.getType()),
						// sensors_per_greenhouse.get(entry.getKey()),
						// actuators.get(entry.getKey()));

						gh_states.remove(Constant.int_hum_type);
						break;
					}
					case (Constant.terrain_hum_type): {
						// Map<String,Actuator> plan_actuators =
						// db.selectPlanActuators(plan.getIdGreenhouse());
						if (gh_states.get(Constant.opt_int_terr_hum_reach) != null) {
							// lo sprinkler viene attivato per meno di mezz'ora, quindi non vi � il bisogno
							// di disattivarlo
							System.out.println("Optimal Terrain Humidity Reached! Deactivating Plan");
							db.deactivatePlan(plan.getId());
						} else {
							// questo caso non si verificher� mai
							System.out.println("Still working for terrain humidity...");
						}
						gh_states.remove(Constant.terrain_hum_type);
						break;
					}
					case (Constant.light_type): {
						if (gh_states.get(Constant.good_light) != null) {
							actions.get(entry.getKey())
									.add(Constant.net + Constant.positive_separator + Constant.planner_off);
							System.out.println("Light Danger Passed");
							db.deactivatePlan(plan.getId());
						} else {
							gh_states.remove(new Integer(Constant.light_danger));
							System.out.println("Still in light danger");
						}
						gh_states.remove(Constant.light_type);
						break;
					}
					case (Constant.wind_type): {
						if (gh_states.get(Constant.good_wind) != null) {
							System.out.println("Wind Danger Passed");
							db.deactivatePlan(plan.getId());
						} else {
							System.out.println("Still in light danger");
						}
						gh_states.remove(Constant.wind_type);
						break;
					}
					}
				}
			}
*/
			// controllo se ci sono situazioni di DANGER! se si, imposto il currentMode a
			// DANGER.
			for (Map.Entry<String, Integer> states : gh_states.entrySet()) {

				switch (states.getValue()) {
				case (Constant.danger_high_int_temp): {

					currentModes.put(entry.getKey(), "DANGER");
					System.out.println("DANGER, high temperature");
				}
				case (Constant.danger_low_int_temp): {

					currentModes.put(entry.getKey(), "DANGER");
					System.out.println("DANGER, low temperature");
				}
				case (Constant.danger_high_int_hum): {

					currentModes.put(entry.getKey(), "DANGER");
					System.out.println("DANGER, high humidity");
				}
				default: { // Constant.danger_low_int_hum

					currentModes.put(entry.getKey(), "DANGER");
					System.out.println("DANGER, low humidity");
				}
				}
			}

			// controllo i problemi, in base alla modalità attivata reagisco
			if (gh_states.size() > 0) {

				for (Map.Entry<String, Integer> problem : gh_states.entrySet()) {

					// Temperatura
					if (problem.getValue() <= Constant.opt_int_temp_reach) {
						
						if(!(gh_states.get(Constant.wind_type) == Constant.wind_danger)) {
							
							switch (currentModes.get(entry.getKey())) {
								case ("ECO"): {
		
									this.activeModality(problem.getValue(), 
											entry.getValue(),
											sensors_per_greenhouse.get(entry.getKey()), 
											actuators.get(entry.getKey()),
											1);
								}
								case ("OPTIMAL"): {
		
									this.activeModality(problem.getValue(), 
											entry.getValue(),
											sensors_per_greenhouse.get(entry.getKey()), 
											actuators.get(entry.getKey()),
											rnd.nextInt(2) + 2);
								}
								case ("DANGER"): {
		
									this.activeModality(problem.getValue(), 
											entry.getValue(),
											sensors_per_greenhouse.get(entry.getKey()), 
											actuators.get(entry.getKey()),
											3);
								}
								default: { //NORMAL
									
									this.activeModality(problem.getValue(), 
											entry.getValue(),
											sensors_per_greenhouse.get(entry.getKey()), 
											actuators.get(entry.getKey()),
											rnd.nextInt(2) + 1 );
									
									actions.get(entry.getKey()).addAll(this.checkReactiveTemperatureUpdates(
											problem.getValue(), 
											entry.getValue(),
											sensors_per_greenhouse.get(entry.getKey()), 
											actuators.get(entry.getKey())));
								}
							}
						} else {
							// se attuatori attivi
							if (actuators.get(entry.getKey()).get(Constant.air_vents).getStatus()) {

								// li disattivo
								actions.get(entry.getKey())
										.add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);
							}
						}
						
						// Umidità
					} else if (problem.getValue() <= Constant.opt_int_hum_reach) {
						
						if(!(gh_states.get(Constant.wind_type) == Constant.wind_danger)) {
							
							switch (currentModes.get(entry.getKey())) {
							case ("ECO"): {
								
								this.activeModality(problem.getValue(), 
										entry.getValue(),
										sensors_per_greenhouse.get(entry.getKey()), 
										actuators.get(entry.getKey()),
										1);
							}
							case ("OPTIMAL"): {
	
								this.activeModality(problem.getValue(), 
										entry.getValue(),
										sensors_per_greenhouse.get(entry.getKey()), 
										actuators.get(entry.getKey()),
										rnd.nextInt(2) + 2);
							}
							case ("DANGER"): {
	
								this.activeModality(problem.getValue(), 
										entry.getValue(),
										sensors_per_greenhouse.get(entry.getKey()), 
										actuators.get(entry.getKey()),
										3);
							}
							default: { //NORMAL
								
								this.activeModality(problem.getValue(), 
										entry.getValue(),
										sensors_per_greenhouse.get(entry.getKey()), 
										actuators.get(entry.getKey()),
										rnd.nextInt(2) + 1 );
									
									actions.get(entry.getKey()).addAll(this.checkReactiveHumidityUpdates(
											problem.getValue(), 
											entry.getValue(),
											sensors_per_greenhouse.get(entry.getKey()), 
											actuators.get(entry.getKey())));
								}
							}
						} else {
							// se attuatori attivi
							if (actuators.get(entry.getKey()).get(Constant.air_vents).getStatus()) {

								// li disattivo
								actions.get(entry.getKey())
										.add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);
							}
						}

						// Luce
					} else if (problem.getValue() <= Constant.good_light) {

						if (problem.getValue() == Constant.light_danger) {
							// se attuatori disattivati
							if (!actuators.get(entry.getKey()).get(Constant.net).getStatus()) {

								// li attivo
								actions.get(entry.getKey())
										.add(Constant.net + Constant.positive_separator + Constant.planner_on);
							}
						}

						// Umidità del Terreno
					} else if (problem.getValue() <= Constant.opt_int_terr_hum_reach) {

						if (problem.getValue() == Constant.low_int_terr_hum) {

							// se attuatori disattivati
							if (!actuators.get(entry.getKey()).get(Constant.sprinkler).getStatus()) {

								// li attivo
								actions.get(entry.getKey())
										.add(Constant.sprinkler + Constant.positive_separator + Constant.planner_on);
							}
						} else {

							switch (currentModes.get(entry.getKey())) {
							case ("ECO"): {
								
								this.activeModality(problem.getValue(), 
										entry.getValue(),
										sensors_per_greenhouse.get(entry.getKey()), 
										actuators.get(entry.getKey()),
										1);
							}
							case ("OPTIMAL"): {
	
								this.activeModality(problem.getValue(), 
										entry.getValue(),
										sensors_per_greenhouse.get(entry.getKey()), 
										actuators.get(entry.getKey()),
										rnd.nextInt(2) + 2);
							}
							case ("DANGER"): {
	
								this.activeModality(problem.getValue(), 
										entry.getValue(),
										sensors_per_greenhouse.get(entry.getKey()), 
										actuators.get(entry.getKey()),
										3);
							}
							default: { //NORMAL
								
								this.activeModality(problem.getValue(), 
										entry.getValue(),
										sensors_per_greenhouse.get(entry.getKey()), 
										actuators.get(entry.getKey()),
										rnd.nextInt(2) + 1 );
								}
							}
						}
						// Vento
					} else if (problem.getValue() <= Constant.good_wind) {

						if (problem.getValue() == Constant.wind_danger) {
							// se attuatori attivi
							if (actuators.get(entry.getKey()).get(Constant.air_vents).getStatus()) {

								// li disattivo
								actions.get(entry.getKey())
										.add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);
							}
						} else {

							switch (currentModes.get(entry.getKey())) {
							case ("ECO"): {
								
								this.activeModality(problem.getValue(), 
										entry.getValue(),
										sensors_per_greenhouse.get(entry.getKey()), 
										actuators.get(entry.getKey()),
										1);
							}
							case ("OPTIMAL"): {
	
								this.activeModality(problem.getValue(), 
										entry.getValue(),
										sensors_per_greenhouse.get(entry.getKey()), 
										actuators.get(entry.getKey()),
										rnd.nextInt(2) + 2);
							}
							case ("DANGER"): {
	
								this.activeModality(problem.getValue(), 
										entry.getValue(),
										sensors_per_greenhouse.get(entry.getKey()), 
										actuators.get(entry.getKey()),
										3);
							}
							default: { //NORMAL
								
								this.activeModality(problem.getValue(), 
										entry.getValue(),
										sensors_per_greenhouse.get(entry.getKey()), 
										actuators.get(entry.getKey()),
										rnd.nextInt(2) + 1 );
								}
							}
						}
					}
				}
			}
		}
		System.out.println("+++++++++++++++++++");
		System.out.println("ACTIONS: " + actions);
		System.out.println("+++++++++++++++++++");
	}
}
