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
	private Random rd = new Random();
	
	public void planning(Map<Integer, HashMap<String,Integer>> greenhouse_states, 
						 Map<Integer, HashMap<String, Sensor>> sensors_per_greenhouse,
						 Map<Integer, HashMap<String, Actuator>> actuators){
		
		Map<Integer,ArrayList<String>> actions = new HashMap<Integer,ArrayList<String>>();
		
		for(Map.Entry<Integer, HashMap<String,Integer>> entry : greenhouse_states.entrySet()){
			
			HashMap<String,Integer> gh_states = entry.getValue();
	
			actions.putIfAbsent(entry.getKey(), new ArrayList<String>());
		
			ArrayList<Plan> active_plans = db.selectActivePlans(entry.getKey());
			
			if (active_plans.size() > 0) {
				for (Plan plan : active_plans){
					
					switch(plan.getType()){
					
						case(Constant.int_temp_type):{
						
							//Map<String,Actuator> plan_actuators = db.selectPlanActuators(plan.getIdGreenhouse()); 
							if (gh_states.get(Constant.opt_int_temp_reach) != null){
								
								actions.get(entry.getKey()).add(Constant.conditioner + Constant.positive_separator+Constant.planner_off);
								System.out.println("Optimal Temperature Reached! Deactivating Plan");
								db.deactivatePlan(plan.getId());
							}else{
								db.updateCurrentVPlan(plan.getId(), sensors_per_greenhouse.get(entry.getKey()).get(Constant.int_temp_type).getValue());
								System.out.println("Still working for temperature..");	
							}
							gh_states.remove(Constant.int_temp_type);
							break;
						}
						case(Constant.int_hum_type):{
							//Map<String,Actuator> plan_actuators = db.selectPlanActuators(plan.getIdGreenhouse()); 
							if (gh_states.get(Constant.opt_int_hum_reach)!=null){
								actions.get(entry.getKey()).add(Constant.humidifier+Constant.positive_separator+Constant.planner_off);
								System.out.println("Optimal Humidity Reached! Deactivating Plan");
								db.deactivatePlan(plan.getId());
							}else{
								db.updateCurrentVPlan(plan.getId(), sensors_per_greenhouse.get(entry.getKey()).get(Constant.int_hum_type).getValue());
								System.out.println("Still working for humidity...");	
							}						
							gh_states.remove(Constant.int_hum_type);
							break;
						}
						case(Constant.terrain_hum_type):{
							//Map<String,Actuator> plan_actuators = db.selectPlanActuators(plan.getIdGreenhouse()); 
							if (gh_states.get(Constant.opt_int_terr_hum_reach)!=null){
								//lo sprinkler viene attivato per meno di mezz'ora, quindi non vi � il bisogno di disattivarlo
								System.out.println("Optimal Terrain Humidity Reached! Deactivating Plan");
								db.deactivatePlan(plan.getId());
							}else{
								//questo caso non si verificher� mai
								System.out.println("Still working for terrain humidity...");	
							}
							gh_states.remove(Constant.terrain_hum_type);
							break;
						}
						case(Constant.light_type):{
							if (gh_states.get(Constant.good_light)!=null){
								actions.get(entry.getKey()).add(Constant.net+Constant.positive_separator+Constant.planner_off);
								System.out.println("Light Danger Passed");
								db.deactivatePlan(plan.getId());
							}else{
								gh_states.remove(new Integer(Constant.light_danger));
								System.out.println("Still in light danger");	
							}
							gh_states.remove(Constant.light_type);
							break;
						}
						case(Constant.wind_type):{
							if (gh_states.get(Constant.good_wind)!= null){
								System.out.println("Wind Danger Passed");
								db.deactivatePlan(plan.getId());
							}else{
								System.out.println("Still in light danger");	
							}
							gh_states.remove(Constant.wind_type);
							break;
						}	
					}
				}
			}	
			
			if (gh_states.size() > 0){
					
				for (Map.Entry<String, Integer> problem : gh_states.entrySet()){
						
					//temperatura
					if (problem.getValue() <= Constant.opt_int_temp_reach){
						if(problem.getValue() == Constant.low_int_temp){	
							//se temperatura esterna < temperatura interna
							if(sensors_per_greenhouse.get(entry.getKey()).get(Constant.ext_temp_type).getValue() <=
									sensors_per_greenhouse.get(entry.getKey()).get(Constant.int_temp_type).getValue()) {
								//se attuatori sportelli attivi
								if(actuators.get(entry.getKey()).get(Constant.air_vents).getStatus()) {
									//chiudo
									actions.get(entry.getKey()).add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);	
								}
							} else {
								//se attuatori sportelli chiusi
								if(!actuators.get(entry.getKey()).get(Constant.air_vents).getStatus()) {
									//apro
									actions.get(entry.getKey()).add(Constant.air_vents + Constant.positive_separator + Constant.planner_on);	
								}
							}
							//se luce è buona
							if(entry.getValue().get(Constant.light_type) == Constant.good_light ) {
								//se attuatori reti attive
								if(actuators.get(entry.getKey()).get(Constant.net).getStatus()) {
									//chiudo
									actions.get(entry.getKey()).add(Constant.net + Constant.positive_separator + Constant.planner_off);	
								}
							}
						/*	
							int power = rd.nextInt(3)+1;
							db.insertPlan(entry.getKey(), Constant.int_temp_type, true,
										sensors_per_greenhouse.get(entry.getKey()).get(Constant.int_temp_type).getValue(), problem.getValue());
							actions.get(entry.getKey()).add(Constant.conditioner+Constant.positive_separator+power);
						*/
						}else if(problem.getValue() == Constant.high_int_temp){
							//se temperatura esterna > temperatura interna
							if(sensors_per_greenhouse.get(entry.getKey()).get(Constant.ext_temp_type).getValue() >
									sensors_per_greenhouse.get(entry.getKey()).get(Constant.int_temp_type).getValue()) {
								//se attuatori sportelli attivi
								if(actuators.get(entry.getKey()).get(Constant.air_vents).getStatus()) {
									//chiudo
									actions.get(entry.getKey()).add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);	
								}
							} else {
								//se attuatori sportelli chiusi
								if(!actuators.get(entry.getKey()).get(Constant.air_vents).getStatus()) {
									//apro
									actions.get(entry.getKey()).add(Constant.air_vents + Constant.positive_separator + Constant.planner_on);	
								}
							}
							//se luce è buona
							if(entry.getValue().get(Constant.light_type) == Constant.good_light ) {
								//se attuatori reti disattivate
								if(!actuators.get(entry.getKey()).get(Constant.net).getStatus()) {
									//apro
									actions.get(entry.getKey()).add(Constant.net + Constant.positive_separator + Constant.planner_on);	
								}
							}
						/*	
							int power = rd.nextInt(3)+1;
							db.insertPlan(entry.getKey(), Constant.int_temp_type, true,
										sensors_per_greenhouse.get(entry.getKey()).get(Constant.int_temp_type).getValue(), problem.getValue());
							actions.get(entry.getKey()).add(Constant.conditioner+Constant.negative_separator+power);
						*/
						
						}else{
							
							//prevision				
				
							
						}
						
					//UMIDITà	
					}else if(problem.getValue() <= Constant.opt_int_hum_reach){
							
						if(problem.getValue() == Constant.low_int_hum){								
							
							//se umidità esterna < umidità interna
							if(sensors_per_greenhouse.get(entry.getKey()).get(Constant.ext_hum_type).getValue() <=
									sensors_per_greenhouse.get(entry.getKey()).get(Constant.int_hum_type).getValue()) {
								//se attuatori sportelli attivi
								if(actuators.get(entry.getKey()).get(Constant.air_vents).getStatus()) {
									//chiudo
									actions.get(entry.getKey()).add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);	
								}
							} else {
								//se attuatori sportelli chiusi
								if(!actuators.get(entry.getKey()).get(Constant.air_vents).getStatus()) {
									//apro
									actions.get(entry.getKey()).add(Constant.air_vents + Constant.positive_separator + Constant.planner_on);	
								}
							}
							
							/*
							int power = rd.nextInt(3)+1;
							db.insertPlan(entry.getKey(), Constant.int_hum_type, true,
										sensors_per_greenhouse.get(entry.getKey()).get(Constant.int_hum_type).getValue(), problem.getValue());
							actions.get(entry.getKey()).add(Constant.humidifier+Constant.positive_separator+power);
							*/
						}else if(problem.getValue() == Constant.high_int_hum){
							
							//se temperatura esterna > temperatura interna
							if(sensors_per_greenhouse.get(entry.getKey()).get(Constant.ext_temp_type).getValue() >
									sensors_per_greenhouse.get(entry.getKey()).get(Constant.int_temp_type).getValue()) {
								//se attuatori sportelli attivi
								if(actuators.get(entry.getKey()).get(Constant.air_vents).getStatus()) {
									//chiudo
									actions.get(entry.getKey()).add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);	
								}
							} else {
								//se attuatori sportelli chiusi
								if(!actuators.get(entry.getKey()).get(Constant.air_vents).getStatus()) {
									//apro
									actions.get(entry.getKey()).add(Constant.air_vents + Constant.positive_separator + Constant.planner_on);	
								}
							}
							/*
							int power = rd.nextInt(3)+1;
							db.insertPlan(entry.getKey(), Constant.int_hum_type, true,
										sensors_per_greenhouse.get(entry.getKey()).get(Constant.int_hum_type).getValue(), problem.getValue());
							actions.get(entry.getKey()).add(Constant.humidifier+Constant.negative_separator+power);
							*/
						}
							
					// Luce
					}else if(problem.getValue() <= Constant.good_light){
						
						if(problem.getValue() == Constant.light_danger){
							
							if(!actuators.get(entry.getKey()).get(Constant.net).getStatus()) {
								
								actions.get(entry.getKey()).add(Constant.net + Constant.positive_separator + Constant.planner_on);
							}
							/* 
							db.insertPlan(entry.getKey(), Constant.light_type, true,
										sensors_per_greenhouse.get(entry.getKey()).get(Constant.light_type).getValue(), problem.getValue());
							actions.get(entry.getKey()).add(Constant.net+Constant.positive_separator+Constant.planner_on);
							 */
						}
							
					// Umidità del Terreno
					}else if(problem.getValue() <= Constant.opt_int_terr_hum_reach) {
							
						if(problem.getValue() == Constant.low_int_terr_hum){
							if(!actuators.get(entry.getKey()).get(Constant.sprinkler).getStatus()) {
								
								actions.get(entry.getKey()).add(Constant.sprinkler + Constant.positive_separator + Constant.planner_on);
							}
							/*
							db.insertPlan(entry.getKey(), Constant.terrain_hum_type, false,
										sensors_per_greenhouse.get(entry.getKey()).get(Constant.terrain_hum_type).getValue(), problem.getValue());
							actions.get(entry.getKey()).add(Constant.sprinkler+Constant.positive_separator+Constant.planner_on);
							*/
						}
							
					//Vento
					}else if(problem.getValue() <= Constant.good_wind){
							
						if(problem.getValue() == Constant.wind_danger){
							if(actuators.get(entry.getKey()).get(Constant.air_vents).getStatus()) {
								
								actions.get(entry.getKey()).add(Constant.air_vents + Constant.positive_separator + Constant.planner_off);
							}
							/*
							db.insertPlan(entry.getKey(), 
										  Constant.wind_type, 
										  true,
										  sensors_per_greenhouse.get(entry.getKey()).get(Constant.wind_type).getValue(), 
										  problem.getValue());
							*/
						}else{
							/* VALUTO SE APRIRE O MENO GLI SPORTELLI
							actions.get(entry.getKey()).add(windAdvantage(greenhouse_states.get(entry.getKey()).get(Constant.int_temp_type),
									greenhouse_states.get(entry.getKey()).get(Constant.int_hum_type),sensors_per_greenhouse.get(entry.getKey())));
							*/
						}
					}		
				}		
			}					
		}	
	}
	
	public String windAdvantage(Integer temp_state, Integer hum_state, HashMap<String, Sensor> sensors){
		
		String wind_action = new String();
		Sensor extTemp = sensors.get(Constant.ext_temp_type);
		Sensor intTemp = sensors.get(Constant.int_temp_type);
		Sensor intHum = sensors.get(Constant.int_hum_type);
		Sensor extHum = sensors.get(Constant.int_hum_type);
		 
		//off = chiuso, on = aperto
		//priorità nell'aumentare la temperatura, rispetto all'umidità
		if(extTemp.getValue() <= intTemp.getValue() ){
			wind_action = Constant.air_vents+Constant.positive_separator+Constant.planner_off;		
		}else if(extTemp.getValue() > intTemp.getValue() + 4){ //per aprire le air_vents, la temperatura deve essere superiore pi� di un clock con condizionatore a potenza massima
			wind_action = Constant.air_vents+Constant.positive_separator+Constant.planner_on;		
		}else  if(extHum.getValue() > intHum.getValue() + 10){
			wind_action = Constant.air_vents+Constant.positive_separator+Constant.planner_on;
		}else{ //extHum.getValue() <= intHum.getValue() //per aprire le air_vents, la temperatura deve essere superiore pi� di un clock con humidifier a potenza massima
			wind_action = Constant.air_vents+Constant.positive_separator+Constant.planner_off;		
		} 
		
		/* priorità all'umidità
		if(extHum.getValue() <= intHum.getValue()){
			wind_action = Constant.air_vents+Constant.positive_separator+Constant.planner_off;		
		}else if(extHum.getValue() > intHum.getValue()){
			wind_action = Constant.air_vents+Constant.positive_separator+Constant.planner_on;		
		}else  if(extTemp.getValue() > intTemp.getValue()){
			wind_action = Constant.air_vents+Constant.positive_separator+Constant.planner_off;
		}else{ //extTemp.getValue() <= intTemp.getValue()
			wind_action = Constant.air_vents+Constant.positive_separator+Constant.planner_on;	
		} 
		*/
		
		
		/*parit� 
		 if(extTemp.getValue() <= intTemp.getValue() && extHum.getValue() <= intHum.getValue()){
			wind_action = Constant.air_vents+Constant.positive_separator+Constant.planner_off;		
		}else {
			wind_action = Constant.air_vents+Constant.positive_separator+Constant.planner_on;		
		} 
		 */
		return wind_action;
		
	}
	
	
	
	public Plan forecastPLan(String type){
		//to do
		return null;
	}
	
	
	/*
	 * Regole da seguire per reagire e per la previsione di plan
	 * 
	 * 
	 * TEMPERATURA INTERNA:::
	 * 
	 */
}

