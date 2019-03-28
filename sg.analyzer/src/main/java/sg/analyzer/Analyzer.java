package sg.analyzer;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import sg.sensor.Sensor;
import sg.actuator.Actuator;
import sg.constant.Constant;
import sg.greenhouse.Greenhouse;
import sg.mysqldb.DBManager;
import sg.planner.Planner;

public class Analyzer {
	private DBManager db = new DBManager();
	private Planner planner = new Planner();

	public void sensorValuesAnalysis(Map<Integer, HashMap<String, Sensor>> sensors_per_greenhouse,
			Map<Integer, HashMap<String, Actuator>> actuators,
			HashMap<Integer, String> currentModes, Calendar clock) {

		
		Map<Integer, HashMap<String, Integer>> greenhouse_problems = new HashMap<Integer, HashMap<String, Integer>>();
		
		for (Map.Entry<Integer, HashMap<String, Sensor>> entry : sensors_per_greenhouse.entrySet()) {
			Greenhouse gh = db.selectGreenhouseById(entry.getKey());
			greenhouse_problems.put(gh.getId(), new HashMap<String, Integer>());

			/* ricavo dai dati dei sensori i codici di stato della serra, in modo da identificarne anticipatamente 
			 * i vari problemi e facilitare il lavoro del planner */
			for (Map.Entry<String, Sensor> s_entry : entry.getValue().entrySet()) {
				Sensor sensor = s_entry.getValue();
				switch (sensor.getType()) {

				//temperatura
				case (Constant.int_temp_type):
					//stati di pericolo
					if (sensor.getValue() <= (gh.getOpt_temp() - Constant.optimal_margin - gh.getRange_temp() - Constant.danger_limit)) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.danger_low_int_temp);
					} else if (sensor.getValue() >= (gh.getOpt_temp() + Constant.optimal_margin + gh.getRange_temp() + Constant.danger_limit)) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.danger_high_int_temp);
					}
					//stati di reazione  
					else if (sensor.getValue() <= (gh.getOpt_temp() - Constant.optimal_margin - gh.getRange_temp())) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.low_int_temp);
					} else if (sensor.getValue() >= (gh.getOpt_temp() + Constant.optimal_margin + gh.getRange_temp())) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.high_int_temp);
					}	
					//stati di predizione 
					else if (sensor.getValue() <= (gh.getOpt_temp() - Constant.optimal_margin)) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.near_low_int_temp);
					} else if (sensor.getValue() >= (gh.getOpt_temp() + Constant.optimal_margin)) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.near_high_int_temp);
					} 
					//stato ottimo
					else {
						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.opt_int_temp_reach);
					}
					break;

				//Umidità
				case (Constant.int_hum_type):
					//stati di pericolo
					if (sensor.getValue() <= (gh.getOpt_hum() - Constant.optimal_margin - gh.getRange_hum() - Constant.danger_limit)) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.danger_low_int_hum);
					} else if (sensor.getValue() >= (gh.getOpt_hum() + Constant.optimal_margin + gh.getRange_hum() + Constant.danger_limit)) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.danger_high_int_hum);
					} 
					//stati di reazione
					else if (sensor.getValue() <= (gh.getOpt_hum() - Constant.optimal_margin - gh.getRange_hum())) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.low_int_hum);
					} else if (sensor.getValue() >= (gh.getOpt_hum() + Constant.optimal_margin + gh.getRange_hum())) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.high_int_hum);
					} 
					//stati di predizione
					else if (sensor.getValue() <= (gh.getOpt_hum() - Constant.optimal_margin)) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.near_low_int_hum);
					} else if (sensor.getValue() >= (gh.getOpt_hum() + Constant.optimal_margin)) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.near_high_int_hum);
					}
					//stato ottimo
					else {
						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.opt_int_hum_reach);
					}
					break;

				//luce esterna
				case (Constant.light_type):

					//luce pericolosa
					if (sensor.getValue() == 3) {
						greenhouse_problems.get(gh.getId()).put(Constant.light_type, Constant.light_danger);
					} 
				
					//luce assente
					else if(sensor.getValue() == 0){
						greenhouse_problems.get(gh.getId()).put(Constant.light_type, Constant.no_light);
					}
					
					//luce moderata
					else{
						greenhouse_problems.get(gh.getId()).put(Constant.light_type, Constant.good_light);
					}
					break;

				//Umidità del terreno
				case (Constant.terrain_hum_type):

					//umidità del terreno insufficiente
					if (sensor.getValue() <= gh.getOpt_terrain_hum() - gh.getRange_terrain_hum()) {
						greenhouse_problems.get(gh.getId()).put(Constant.terrain_hum_type, Constant.low_int_terr_hum);
					}
					//umidità del terreno non ottimale
					else if (sensor.getValue() <= (gh.getOpt_terrain_hum() - Constant.optimal_margin)) {
						greenhouse_problems.get(gh.getId()).put(Constant.terrain_hum_type, Constant.near_low_int_hum);
					}
					//umidità del terreno ottimale
					else {
						greenhouse_problems.get(gh.getId()).put(Constant.terrain_hum_type,
								Constant.opt_int_terr_hum_reach);
					}
					break;

					
				//Vento
				case (Constant.wind_type):
					
					//vento pericoloso
					if (sensor.getValue() == 3) {
						greenhouse_problems.get(gh.getId()).put(Constant.wind_type, Constant.wind_danger);
					} 
				
					//vento non pericoloso
					else {
						greenhouse_problems.get(gh.getId()).put(Constant.wind_type, Constant.good_wind);
					}
					break;
				}
			}
		}
		
		planner.planning(greenhouse_problems, sensors_per_greenhouse, actuators, currentModes, clock);

	}
}