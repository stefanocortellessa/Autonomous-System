package sg.constant;

public final class Constant {

	// ids mqtt
	public static final String monitor_receiver = "monitor_receiver";
	public static final String monitor_sender = "monitor_sender";
	public static final String mode_receiver = "mode_receiver";
	public static final String executor_sender = "executor_sender";
	public static final String executor_receiver = "executor_receiver";
	public static final String simulator_sender = "simulator_sender";
	public static final String updater_receiver = "updater_receiver";
	public static final String updater_sender = "updater_sender";
	public static final String monitor_channel = "monitor/greenhouse/+/sensor/#";
	public static final String actuator_channel = "monitor/greenhouse/+/actuator/";
	public static final String simulator_channel = "monitor/simulator";
	public static final String updater_channel = "openHab/executor/greenhouse/+/actuator/#";
	public static final String executor_channel = "openHab/executor/greenhouse/+/actuator/#";
	public static final String mode_channel = "openHab/mode";
	public static final String int_temp_type = "intTemp";
	public static final String ext_temp_type = "extTemp";
	public static final String ext_hum_type = "extHum";
	public static final String int_hum_type = "intHum";
	public static final String light_type = "light";
	public static final String wind_type = "wind";
	public static final String terrain_hum_type = "grHum";
	public static final String rain_type = "rain";
	// types of actuators
	public static final String conditioner = "temp";
	public static final String humidifier = "hum";
	public static final String sprinkler = "wat";
	public static final String air_vents = "wind";
	public static final String net = "light";
	// problems
	/*
	 * 11 -> Temperatura bassa 12 -> Temperatura alta 13 -> Buona Temperatura 21 ->
	 * Umidità bassa 22 -> Umidità alta 23 -> Buona Umidità 32 -> luce alta 33 ->
	 * Buona Luce 41 -> Umidità Terreno bassa 42 -> Umidità Terreno alta 43 -> Buona
	 * Umidità Terreno 52 -> Vento forte 53 -> Vento stabile
	 * 
	 * Il primo numero identifica il tipo di sensore (1 temperatura, 2 umidità, 3
	 * luce, 4 umidità del terreno, 5 vento) Il secondo numero identifica il tipo di
	 * pericolo (1 sotto il range, 2 sopra il range)
	 */
	// temp
	public static final int danger_low_int_temp = 11;
	public static final int danger_high_int_temp = 12;
	public static final int low_int_temp = 13;
	public static final int high_int_temp = 14;
	public static final int near_low_int_temp = 15;
	public static final int near_high_int_temp = 16;
	public static final int opt_int_temp_reach = 17;
	// hum
	public static final int danger_low_int_hum = 21;
	public static final int danger_high_int_hum = 22;
	public static final int low_int_hum = 23;
	public static final int high_int_hum = 24;
	public static final int near_low_int_hum = 25;
	public static final int near_high_int_hum = 26;
	public static final int opt_int_hum_reach = 27;
	
	public static final int light_danger = 32;
	public static final int good_light = 33;
	public static final int low_int_terr_hum = 41;
	public static final int high_int_terr_hum = 42;
	public static final int opt_int_terr_hum_reach = 43;
	public static final int wind_danger = 52;
	public static final int good_wind = 53;
	// planner on-off
	public static final String planner_off = "0";
	public static final String planner_on = "1";
	// planner separators
	public static final String positive_separator = ",";
	public static final String negative_separator = ",-";
	// planner margin
	public static final int optimal_temp_margin = 2;
	public static final int optimal_hum_margin = 10;
	// danger limits
	public static final int danger_temp_limit = 5;
	public static final int danger_hum_limit = 20;
	
	public static final int thread_activation = 5000;
	
	

	public final static String[] parse_message(String s) {
		return s.split(",");
	}

	public final static String[] parse_message2(String s) {
		return s.split("/");
	}

	public final static int get_sensor_id_from_message(String s) {
		return new Integer(parse_message(s)[0]);
	}

	public final static String get_sensor_name_from_message(String s) {
		return parse_message(s)[1];
	}

	public final static String get_sensor_type_from_message(String s) {
		return parse_message(s)[2];
	}

	public final static double get_sensor_val_from_message(String s) {
		return new Double(parse_message(s)[3]);
	}

	public final static int get_gh_id_from_message(String s) {
		return new Integer(parse_message(s)[4]);
	}

	public final static int get_id_greenhouse(String s) {
		return new Integer(parse_message2(s)[3]);
	}

	public final static String get_actuator_type(String s) {
		return new String(parse_message2(s)[5]);
	}
}
