package sg.knowledge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import sg.actuator.Actuator;
import sg.constant.Constant;
import sg.mysqldb.DBManager;
import sg.sensor.Sensor;
import sg.history.History;

public class Knowledge {
	private DBManager db = new DBManager();
	
	public Knowledge(){}
	
	
	public void insertRecords(Map<Integer,HashMap<String,Sensor>> sensors, Map<Integer,HashMap<String,Actuator>> actuators, Calendar clock){
		
		//genero per ogni greenhouse il corrispondente oggetto History
		Map <Integer,History> history_records = new HashMap<Integer,History>();
		for (Map.Entry<Integer,HashMap<String, Sensor>>entry : sensors.entrySet()){
			
			//sensori
			history_records.putIfAbsent(entry.getKey(), new History());
			history_records.get(entry.getKey()).setIntemp(entry.getValue().get(Constant.int_temp_type).getValue());
			history_records.get(entry.getKey()).setExtTemp(entry.getValue().get(Constant.ext_temp_type).getValue());
			history_records.get(entry.getKey()).setIntHum(entry.getValue().get(Constant.int_hum_type).getValue());
			history_records.get(entry.getKey()).setExtHum(entry.getValue().get(Constant.ext_hum_type).getValue());
			history_records.get(entry.getKey()).setWind(entry.getValue().get(Constant.wind_type).getValue());
			history_records.get(entry.getKey()).setRain(entry.getValue().get(Constant.rain_type).getValue());
			history_records.get(entry.getKey()).setLight(entry.getValue().get(Constant.light_type).getValue());
			history_records.get(entry.getKey()).setGrHum(entry.getValue().get(Constant.terrain_hum_type).getValue());
			//actuatori
			history_records.get(entry.getKey()).setAir_vents(actuators.get(entry.getKey()).get(Constant.air_vents).getPower());
			history_records.get(entry.getKey()).setHumidifier(actuators.get(entry.getKey()).get(Constant.humidifier).getPower());
			history_records.get(entry.getKey()).setConditioner(actuators.get(entry.getKey()).get(Constant.conditioner).getPower());
			history_records.get(entry.getKey()).setNet(actuators.get(entry.getKey()).get(Constant.net).getPower());
			history_records.get(entry.getKey()).setSprinkler(actuators.get(entry.getKey()).get(Constant.sprinkler).getPower());
			//setto l'id della serra ed il timestamp generato
			history_records.get(entry.getKey()).setDatetime(clock);
			history_records.get(entry.getKey()).setId_greenhouse(entry.getKey());
		}
		
		//inserisco i record nella tabella History del Database
		db.insertHistoryRecords(history_records);
	}
	
	
	public boolean predictiveLowTemperaturePlan(int idGh, Calendar datetime){
		
		/*ottengo dal database lo storico relativo all'ultimo mese, considerando un intervallo orario 
		 * compreso tra l'ora attuale e le tre ore sucessive */
		Map<Calendar,ArrayList<History>> lastMonth = db.selectLastMonthHistoryG(idGh, datetime);
		
		//se sono presenti dati per meno di un mese, non è possibile effettuare la predizione, quindi la interrompo
		if (lastMonth.size()<30) return false;
		
		//questo valore rappresenta il numero di giorni in cui si è verificato un andamento medio decrescente 
		int similarity = 0;
		
		/* per ogni giorno, controllo che nelle tre ore successive si sia verificato un decremento della temperatura
		 * per almeno 2/3 dell'arco di tempo considerato. Se così fosse, allora verrà preventivamente attivato un piano,
		 * in modo da mantenere la serra nel range di temperatura desiderato */
		for (Map.Entry<Calendar, ArrayList<History>> dayRecords : lastMonth.entrySet()){
	
			ArrayList<History> records = dayRecords.getValue();
	
			/* calcolo l'andamento medio della temperatura esterna nelle tre ore successive. 
			 * Se la temperatura esterna attuale è maggiore dell'andamento medio appena calcolato, vorrà dire
			 * che probabilmente ci sarà un abbassamento della temperatura esterna. 
			 * Quindi, questo giorno viene considerato*/
			double avg_trend = records.stream().mapToDouble(record -> record.getExtTemp()).sum()/records.size();
	
			/*per evitare di considerare giornate in cui vi è una differenza irrisoria tra il valore attuale
			 * e l'andamento medio, viene considerato un fattore di tolleranza pari a 0.5 gradi */
			if (records.get(0).getExtTemp() > avg_trend + 0.5){
				similarity++;
			}
		
		}
		if(similarity>=Math.round(lastMonth.size()*(2.0/3.0)))
			return true;
			
		return false;
			
	}
	
	public boolean predictiveHighTemperaturePlan(int idGh, Calendar datetime){
		
		/*ottengo dal database lo storico relativo all'ultimo mese, considerando un intervallo orario 
		 * compreso tra l'ora attuale e le tre ore sucessive */
		Map<Calendar,ArrayList<History>> lastMonth = db.selectLastMonthHistoryG(idGh, datetime);

		//se sono presenti dati per meno di un mese, non è possibile effettuare la predizione, quindi la interrompo
		if (lastMonth.size()<30) return false;
		//questo valore rappresenta il numero di giorni in cui si è verificato un andamento decrescente/crescente 
		int similarity = 0;
		
		
		/* per ogni giorno, controllo che nelle tre ore successive si sia verificato un aumento della temperatura
		 * per almeno 2/3 dell'arco di tempo considerato. Se così fosse, allora verrà preventivamente attivato un piano,
		 * in modo da mantenere la serra nel range di temperatura desiderato */
		for (Map.Entry<Calendar, ArrayList<History>> dayRecords : lastMonth.entrySet()){
	
			ArrayList<History> records = dayRecords.getValue();
			
			/* calcolo l'andamento medio della temperatura esterna nelle tre ore successive. 
			 * Se la temperatura esterna attuale è minore dell'andamento medio appena calcolato, vorrà dire
			 * che probabilmente ci sarà un innalzamento della temperatura esterna. 
			 * Quindi, questo giorno viene considerato*/
			double avg_trend = records.stream().mapToDouble(record -> record.getExtTemp()).sum()/records.size();
	
			/*per evitare di considerare giornate in cui vi è una differenza irrisoria tra il valore attuale
			 * e l'andamento medio, viene considerato un fattore di tolleranza pari a 0.5 gradi */
			if (records.get(0).getExtTemp() < avg_trend - 0.5){
				similarity++;
			}
		}
			
		if(similarity>=Math.round(lastMonth.size()*(2.0/3.0)))
			return true;
			
		return false;
			
	}
	
	
	public boolean predictiveLowHumidityPlan(int idGh, Calendar datetime){
		
		/*ottengo dal database lo storico relativo all'ultimo mese, considerando un intervallo orario 
		 * compreso tra l'ora attuale e le tre ore sucessive */
		Map<Calendar,ArrayList<History>> lastMonth = db.selectLastMonthHistoryG(idGh, datetime);
	
		//se sono presenti dati per meno di un mese, non è possibile effettuare la predizione, quindi la interrompo
		if (lastMonth.size()<30) return false;
		
		//questo valore rappresenta il numero di giorni in cui si è verificato un andamento decrescente/crescente 
		int similarity = 0;
		
		/* per ogni giorno, controllo che nelle tre ore successive si sia verificato un decremento dell'umidità
		 * per almeno 2/3 dell'arco di tempo considerato. Se così fosse, allora verrà preventivamente attivato un piano,
		 * in modo da mantenere la serra nel range di umidità desiderato */
		for (Map.Entry<Calendar, ArrayList<History>> dayRecords : lastMonth.entrySet()){
	
			ArrayList<History> records = dayRecords.getValue();
			
			/* calcolo l'andamento medio dell'umidità esterna nelle tre ore successive. 
			 * Se l'umidità esterna attuale è maggiore dell'andamento medio appena calcolato, vorrà dire
			 * che probabilmente ci sarà un abbassamento dell'umidità esterna. 
			 * Quindi, questo giorno viene considerato*/
			double avg_trend = records.stream().mapToDouble(record -> record.getExtHum()).sum()/records.size();
	
			/*per evitare di considerare giornate in cui vi è una differenza irrisoria tra il valore attuale
			 * e l'andamento medio, viene considerato un fattore di tolleranza pari ad un punto percentuale */
			if (records.get(0).getExtHum() > avg_trend + 1){
				similarity++;
			}
		}
			
		if(similarity>=Math.round(lastMonth.size()*(2.0/3.0)))
			return true;
			
		return false;	
	}
	
	public boolean predictiveHighHumidityPlan(int idGh, Calendar datetime){
		
		/*ottengo dal database lo storico relativo all'ultimo mese, considerando un intervallo orario 
		 * compreso tra l'ora attuale e le tre ore sucessive */
		Map<Calendar,ArrayList<History>> lastMonth = db.selectLastMonthHistoryG(idGh, datetime);

		//se sono presenti dati per meno di un mese, non è possibile effettuare la predizione, quindi la interrompo
		if (lastMonth.size()<30) return false;
		
		//questo valore rappresenta il numero di giorni in cui si è verificato un andamento decrescente/crescente 
		int similarity = 0;
			
		/* per ogni giorno, controllo che nelle tre ore successive si sia verificato un aumento dell'umidità
		* per almeno 2/3 dell'arco di tempo considerato. Se così fosse, allora verrà preventivamente attivato un piano,
		* in modo da mantenere la serra nel range di umidità desiderato */
		for (Map.Entry<Calendar, ArrayList<History>> dayRecords : lastMonth.entrySet()){
	
			ArrayList<History> records = dayRecords.getValue();
			
			/* calcolo l'andamento medio dell'umidità esterna nelle tre ore successive. 
			 * Se l'umidità esterna attuale è minore dell'andamento medio appena calcolato, vorrà dire
			 * che probabilmente ci sarà un innalzamento dell'umidità esterna. 
			 * Quindi, questo giorno viene considerato*/
			double avg_trend = records.stream().mapToDouble(record -> record.getExtHum()).sum()/records.size();
	
			/*per evitare di considerare giornate in cui vi è una differenza irrisoria tra il valore attuale
			 * e l'andamento medio, viene considerato un fattore di tolleranza pari ad un punto percentuale */
			if (records.get(0).getExtHum() < avg_trend - 1){
				similarity++;
			}
		}
			
		if(similarity>=Math.round(lastMonth.size()*(2.0/3.0)))
			return true;
			
		return false;
	}
	
	
}




