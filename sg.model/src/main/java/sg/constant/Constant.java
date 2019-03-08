package sg.constant;

public final class Constant {
	
	//ids mqtt
	public static final String monitor_receiver_id = "monitor_receiver";
	public static final String monitor_sender_id = "monitor_sender";
	public static final String executor_sender_id = "executor_sender";
	public static final String simulator_sender_id = "simulator_sender";
	public static final String monitor_channel = "monitor/greenhouse/+/sensor/#";
	public static final String actuator_channel = "monitor/greenhouse/+/actuator/";
	public static final String simulator_channel = "monitor/simulator";
	public static final String int_temp_type = "intTemp";
	public static final String ext_temp_type = "extTemp";
	public static final String ext_hum_type = "extHum";
	public static final String int_hum_type = "intHum";
	public static final String light_type = "light";
	public static final String wind_type = "wind";
	public static final String terrain_hum_type = "grdHum";
	public static final String rain_type = "rain";
	
	public static final int thread_activation = 5000;
	
	public final static String[] parse_message(String s){
		return s.split(",");
	}
	
	public final static int get_sensor_id_from_message(String s){
		return new Integer(parse_message(s)[0]);
	}
	
	public final static String get_sensor_name_from_message(String s){
		return parse_message(s)[1];
	}
	
	public final static String get_sensor_type_from_message(String s){
		return parse_message(s)[2];
	}
	
	public final static double get_sensor_val_from_message(String s){
			return new Double(parse_message(s)[3]);
	}	
	
	public final static int get_gh_id_from_message(String s){
		return new Integer(parse_message(s)[4]);
	}
}
