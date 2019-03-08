package sg.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import sg.sensor.Sensor;
import sg.constant.Constant;
import sg.greenhouse.Greenhouse;
import sg.mysqldb.DBManager;

public class Analyzer{
    private DBManager db = new DBManager();
    //private Planner planner = new Planner();
    
    
    /*
     * 11 -> Temperatura bassa
     * 12 -> Temperatura alta
     * 13 -> Buona Temperatura 
     * 21 -> Umidità bassa
     * 22 -> Umidità alta
     * 23 -> Buona Umidità
     * 32 -> luce alta
     * 33 -> Buona Luce
     * 41 -> Umidità Terreno bassa
     * 42 -> Umidità Terreno alta
     * 43 -> Buona Umidità Terreno
     * 52 -> Vento forte
     * 53 -> Vento stabile
     * 
     * Il primo numero identifica il tipo di sensore (1 temperatura, 2 umidità, 3 luce, 4 umidità del terreno, 5 vento)
     * Il secondo numero identifica il tipo di pericolo (1 sotto il range, 2 sopra il range)
     * */
    public void sensorValuesAnalysis(Map<Integer,ArrayList<Sensor>> sensors_per_greenhouse){
    	
		Map<Integer, ArrayList<String>> greenhouse_problems = new HashMap<Integer,ArrayList<String>>();
    	for (Map.Entry<Integer, ArrayList<Sensor>> entry : sensors_per_greenhouse.entrySet()){
    		
    		Greenhouse gh = db.selectGreenhouseById(entry.getKey());
			greenhouse_problems.put(gh.getId(), new ArrayList<String>());
    		for (Sensor sensor : entry.getValue()){
    			
    			switch(sensor.getType()){
    				
	    			case(Constant.int_temp_type):
	    				if (sensor.getValue() <= (gh.getOpt_temp() - gh.getRange_temp())){
	    					
	    					greenhouse_problems.get(gh.getId()).add("11");
	    					System.out.println("Danger: Low Temperature");
	    					
	    				}else if(sensor.getValue() >= (gh.getOpt_temp() + gh.getRange_temp())) {
    					
	    					greenhouse_problems.get(gh.getId()).add("12");
	    					System.out.println("Danger: High Temperature");
	    				}else {
	    					
	    					greenhouse_problems.get(gh.getId()).add("13");
	    					System.out.println("Good Internal Temperature Reached");
	    				}	
	    				break;
	    				
	    			case(Constant.int_hum_type):
	    				
	    				if (sensor.getValue() <= (gh.getOpt_hum() - gh.getRange_hum())){
	    					
	    					greenhouse_problems.get(gh.getId()).add("21");
	    					System.out.println("Danger: Low Humidity");
	    					
	    				}else if(sensor.getValue() >= (gh.getOpt_hum() + gh.getRange_hum())) {
    					
	    					greenhouse_problems.get(gh.getId()).add("22");
	    					System.out.println("Danger: High Humidity");
	    				}else {
	    					greenhouse_problems.get(gh.getId()).add("23");
	    					System.out.println("Good Internal Humidity Reached");
	    				}	
	    				break;
    			
	    			case(Constant.light_type):
	    				
	    				if(sensor.getValue() >= gh.getOpt_light()) {
	    					
	    					greenhouse_problems.get(gh.getId()).add("32");
	    					System.out.println("Danger: High Light Intensity");
	    				}else {
	    					greenhouse_problems.get(gh.getId()).add("33");
	    					System.out.println("Good Internal Light Intensity Reached");
	    				}	
	    				break;
    			
	    			case(Constant.terrain_hum_type):

	    				if (sensor.getValue() <= (gh.getOpt_terrain_hum() - gh.getRange_terrain_hum())){
	    					
	    					greenhouse_problems.get(gh.getId()).add("41");
	    					System.out.println("Danger: Low Ground Humidity");
	    				}else if(sensor.getValue() >= (gh.getOpt_terrain_hum() + gh.getRange_terrain_hum())) {
    					
	    					greenhouse_problems.get(gh.getId()).add("42");
	    					System.out.println("Danger: High Ground Humidity");
	    				}else {
	    					
	    					greenhouse_problems.get(gh.getId()).add("43");
	    					System.out.println("Good Internal Ground Humidity Reached");
	    				}	
	    				break;
	    				
	    			default: //Constant.wind_type
	    				if (sensor.getValue() == 3){ 
	    					
	    					greenhouse_problems.get(gh.getId()).add("52");  
	    					System.out.println("Danger: Wind too strong");
	    					
	    				}else {
	    					greenhouse_problems.get(gh.getId()).add("53"); 
	    					System.out.println("Not dangerous wind");
	    				}
	    				break;
    			}
    		}
    		
    		if (greenhouse_problems.get(gh.getId()) != null && greenhouse_problems.get(gh.getId()).size() > 0){
    			System.out.println("Greenhouse " + gh.getId() + " Problems: " + greenhouse_problems.get(gh.getId()));
    			
    		}	
    	}    	
    	
    	//PASSARE QUI IL AL PLANNER greenhouse_problems CONTENENTE I CODICI DELLE SITUAZIONI ATTUALI
    	
    	System.out.println("");
    }    
}
