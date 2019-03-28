package sg.constant;

import java.util.ArrayList;
import java.util.HashMap;

public final class Constant {

 //canali mqtt
 public static final String monitor_sensor_channel = "monitor/greenhouse/+/sensor/#";
 public static final String monitor_actuator_channel = "monitor/greenhouse/+/actuator/#";
 public static final String openhab_sensor_channel = "openHab/greenhouse/+/sensor/#";
 public static final String openhab_actuator_channel = "openHab/greenhouse/+/actuator/#";
 public static final String executor_channel = "executor/farm/greenhouse/+/actuator/#";
 public static final String updater_channel = "openHab/executor/greenhouse/+/actuator/#";
 public static final String clock_channel = "openHab/greenhouse/time";
 public static final String mode_channel = "openHab/mode/greenhouse/#";
 public static final String alert_channel = "openHab/alert/greenhouse/#";
 
 //tipi dei sensori
 public static final String int_temp_type = "intTemp";
 public static final String ext_temp_type = "extTemp";
 public static final String ext_hum_type = "extHum";
 public static final String int_hum_type = "intHum";
 public static final String light_type = "light";
 public static final String wind_type = "wind";
 public static final String terrain_hum_type = "grHum";
 public static final String rain_type = "rain";
 
 // tipi di attuatori
 public static final String conditioner = "conditioner";
 public static final String humidifier = "humidifier";
 public static final String sprinkler = "sprinkler";
 public static final String air_vents = "air_vents";
 public static final String net = "net";
 
 
 // codici dei problemi relativi ai tipi dei sensor

 // temperatura
 public static final int danger_low_int_temp = 11;
 public static final int danger_high_int_temp = 12;
 public static final int low_int_temp = 13;
 public static final int high_int_temp = 14;
 public static final int near_low_int_temp = 15;
 public static final int near_high_int_temp = 16;
 public static final int opt_int_temp_reach = 17;
 
 // umidità
 public static final int danger_low_int_hum = 21;
 public static final int danger_high_int_hum = 22;
 public static final int low_int_hum = 23;
 public static final int high_int_hum = 24;
 public static final int near_low_int_hum = 25;
 public static final int near_high_int_hum = 26;
 public static final int opt_int_hum_reach = 27;
 
 //luce esterna
 public static final int no_light = 31;
 public static final int light_danger = 32;
 public static final int good_light = 33;
 
 //umidità del terreno
 public static final int low_int_terr_hum = 41;
 public static final int near_int_terr_hum = 42;
 public static final int opt_int_terr_hum_reach = 43;
 
 //vento
 public static final int wind_danger = 52;
 public static final int good_wind = 53;
 
 // attivazione/disattivazione attuatore
 public static final String off = "0";
 public static final String on = "1";
 
 // separatori di stringhe 
 public static final String sep = ",";
 public static final String neg_sep = ",-";
 
 // margine dell'intervallo ottimo
 public static final int optimal_margin = 2;
 
 // limiti dello stato di pericolo
 public static final int danger_limit = 5;
 
 //modalities
 public static final String eco_mode = "0";
 public static final String normal_mode = "1";
 public static final String optimal_mode = "2";
 public static final String danger_mode = "3";
 
 @SuppressWarnings("serial")
 //mappa che fa corrispondere ad ogni modalità il proprio range di valori
 public static final HashMap<String, ArrayList<Integer>> modes = new HashMap<String, ArrayList<Integer>>()
 {{
      put(eco_mode, new ArrayList<Integer>()       {{add(1);}}   );
      put(normal_mode, new ArrayList<Integer>()    {{add(1);add(2);}}   );
      put(optimal_mode, new ArrayList<Integer>()   {{add(2);add(3);}}  );
      put(danger_mode, new ArrayList<Integer>()    {{add(3);}}   );
 }};
 
 
 //metodi di parsing
 public final static String[] parseMessage(String s) {
  return s.split(",");
 }

 public final static String[] parseTopic(String s) {
  return s.split("/");
 }

 public final static int get_element_id_from_message(String s) {
  return new Integer(parseMessage(s)[0]);
 }

 public final static String get_element_name_from_message(String s) {
  return parseMessage(s)[1];
 }

 public final static String get_element_type_from_message(String s) {
  return parseMessage(s)[2];
 }

 public final static double get_sensor_val_from_message(String s) {
  return new Double(parseMessage(s)[3]);
 }

 public final static int get_actuator_val_from_message(String s) {
		return new Integer(parseMessage(s)[3]);
	}

 public final static int get_gh_id_from_message(String s) {
  return new Integer(parseMessage(s)[4]);
 }

 public final static int get_id_greenhouse(String s) {
  return new Integer(parseTopic(s)[3]);
 }

 public final static String get_actuator_type(String s) {
  String[] parsed = parseTopic(s);
  return parsed[parsed.length-1];
 }
 
 
 
 
}
