package sg.analyzer;

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
			HashMap<Integer, HashMap<String, Actuator>> actuators,
			HashMap<Integer, String> currentModes) {

		Map<Integer, HashMap<String, Integer>> greenhouse_problems = new HashMap<Integer, HashMap<String, Integer>>();
		for (Map.Entry<Integer, HashMap<String, Sensor>> entry : sensors_per_greenhouse.entrySet()) {
			Greenhouse gh = db.selectGreenhouseById(entry.getKey());
			greenhouse_problems.put(gh.getId(), new HashMap<String, Integer>());

			for (Map.Entry<String, Sensor> s_entry : entry.getValue().entrySet()) {
				Sensor sensor = s_entry.getValue();
				switch (sensor.getType()) {

				case (Constant.int_temp_type):
					if (sensor.getValue() <= (gh.getOpt_temp() - gh.getRange_temp() - Constant.danger_temp_limit)) {

						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.danger_low_int_temp);
						System.out.println("Danger: Low Temperature");

					} else if (sensor
							.getValue() >= (gh.getOpt_temp() + gh.getRange_temp() + Constant.danger_temp_limit)) {

						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.danger_high_int_temp);
						System.out.println("Danger: High Temperature");

					} else if (sensor.getValue() <= (gh.getOpt_temp() - gh.getRange_temp())) {

						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.low_int_temp);
						System.out.println("Reaction Low Temperature");

					} else if (sensor.getValue() >= (gh.getOpt_temp() + gh.getRange_temp())) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.high_int_temp);
						System.out.println("Reaction High Temperature");
					} else if (sensor.getValue() <= (gh.getOpt_temp() - Constant.optimal_temp_margin)) {

						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.near_low_int_temp);
						System.out.println("Reaching Low Temperature");

					} else if (sensor.getValue() >= (gh.getOpt_temp() + Constant.optimal_temp_margin)) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.near_high_int_temp);
						System.out.println("Reaching High Temperature");
					} else {
						// intervallo ottimo
						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.opt_int_temp_reach);
						System.out.println("Optima Internal Temperature Range Reached");
					}
					break;

				case (Constant.int_hum_type):

					if (sensor.getValue() <= (gh.getOpt_hum() - gh.getRange_hum() - Constant.danger_hum_limit)) {

						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.danger_low_int_hum);
						System.out.println("Danger: Low Humidity");

					} else if (sensor.getValue() >= (gh.getOpt_hum() + gh.getRange_hum() + Constant.danger_hum_limit)) {

						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.danger_high_int_hum);
						System.out.println("Danger: High Humidity");

					} else if (sensor.getValue() <= (gh.getOpt_hum() - gh.getRange_hum())) {

						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.low_int_hum);
						System.out.println("Reaction Low Humidity");

					} else if (sensor.getValue() >= (gh.getOpt_hum() + gh.getRange_hum())) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.high_int_hum);
						System.out.println("Reaction High Humidity");
					} else if (sensor.getValue() <= (gh.getOpt_hum() - Constant.optimal_hum_margin)) {

						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.near_low_int_hum);
						System.out.println("Reaching Low Humidity");

					} else if (sensor.getValue() >= (gh.getOpt_hum() + Constant.optimal_hum_margin)) {
						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.near_high_int_hum);
						System.out.println("Reaching High Humidity");
					} else {
						// intervallo ottimo
						greenhouse_problems.get(gh.getId()).put(Constant.int_hum_type, Constant.opt_int_hum_reach);
						System.out.println("Optima Internal Humidity Range Reached");
					}
					break;

				case (Constant.light_type):

					if (sensor.getValue() >= gh.getOpt_light()) {

						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.light_danger);
						System.out.println("Danger: High Light Intensity");
					} else {
						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.good_light);
						System.out.println("Good Internal Light Intensity Reached");
					}
					break;

				case (Constant.terrain_hum_type):

					if (sensor.getValue() <= (gh.getOpt_terrain_hum() - gh.getRange_terrain_hum())) {

						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.low_int_terr_hum);
						System.out.println("Danger: Low Ground Humidity");
					} else {

						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type,
								Constant.opt_int_terr_hum_reach);
						System.out.println("Good Internal Ground Humidity Reached");
					}
					break;

				default: // Constant.wind_type
					if (sensor.getValue() == 3) {

						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.wind_danger);
						System.out.println("Danger: Wind too strong");

					} else {
						greenhouse_problems.get(gh.getId()).put(Constant.int_temp_type, Constant.good_wind);
						System.out.println("Not dangerous wind");
					}
					break;
				}
			}

			if (greenhouse_problems.get(gh.getId()) != null && greenhouse_problems.get(gh.getId()).size() > 0) {
				System.out.println("Greenhouse " + gh.getId() + " Problems: " + greenhouse_problems.get(gh.getId()));

			}
		}
		
		planner.planning(greenhouse_problems, sensors_per_greenhouse, actuators, currentModes);

		System.out.println("");
	}
}