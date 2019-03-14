package sg.simulator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;

import sg.paho.PahoCommunicator;
import sg.greenhouse.*;
import sg.sensor.*;
import sg.actuator.*;
import sg.constant.Constant;
import sg.mysqldb.*;

public class Simulator extends Thread {

	private ArrayList<Greenhouse> greenhouses;
	private Calendar clock = new GregorianCalendar();
	private PahoCommunicator paho = new PahoCommunicator();
	
	private volatile boolean active = true;
	private double hour = 0;
	private double extTemp = 15;
	private double intTemp;
	private double extHum = 64;
	private double intHum;
	private double grdHum = 45;
	private int extLight;
	private int extWind = 0;
	private int rain = 1;

	public void run() {

		Calendar.getInstance();
		clock.set(Calendar.HOUR_OF_DAY, 0);
		clock.set(Calendar.MINUTE, 0);

		while (active) {
			try {

				// aggiungiamo 30 minuti ogni thread che parte
				clock.add(Calendar.MINUTE, 30);
				System.out.println("------------");
				System.out.println("CLOCK: " + clock.get(Calendar.HOUR_OF_DAY) + ":" + clock.get(Calendar.MINUTE));
				System.out.println("------------");
				hour = clock.get(Calendar.HOUR_OF_DAY);

				this.simulation();

				Thread.sleep(Constant.thread_activation);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void simulation() {

		HashMap<Integer, ArrayList<Sensor>> sensors = new HashMap<Integer, ArrayList<Sensor>>();
		HashMap<Integer, HashMap<String, Actuator>> actuators = new HashMap<Integer, HashMap<String, Actuator>>();
		DBManager dbm = new DBManager();

		greenhouses = dbm.selectAllGreenhouses();

		this.rain = this.setRain();

		for (Greenhouse gh : greenhouses) {

			System.out.println("id_greenhouse: " + gh.getId());
			sensors.put(gh.getId(), dbm.selectGreenhouseSensors(gh.getId()));
			actuators.put(gh.getId(), dbm.selectGreenhouseActuatorsType(gh.getId()));
		}

		this.extTemp = this.setExternalTemperature();
		this.extHum = this.setExternalHumidity();
		this.extLight = this.setLight();
		this.extWind = this.setWind();

		/*
		 * per ogni sensore di ogni Greenhouse, ogni mezz'ora aggiorno i dati dei valori
		 * rilevati nel Database
		 */
		for (Greenhouse gh : greenhouses) {

			for (Sensor sns : sensors.get(gh.getId())) {
				this.updateSensorValues(sns, dbm, gh, actuators.get(gh.getId()), 
						this.extTemp, this.extHum, this.extLight, this.extWind);
			}
			
		}
	}

	/*
	 * Genera valori random per tutti i sensori e li metto nel DB
	 */
	public void updateSensorValues(Sensor sns, DBManager dbm, Greenhouse gh, HashMap<String, Actuator> actuators,
			double extTemp, double extHum, double extLight, int extWind) {

		// System.out.println("TIPO SENSORE: " + sns.getType());
		switch (sns.getType()) {
		case "extHum":
			if (sns.getStatus()) {
				dbm.updateSensor(sns.getId(), gh.getId(), extHum, sns.getStatus(), sns.getName());
			}
			break;
		case "intHum":
			if (sns.getStatus()) {
				dbm.updateSensor(sns.getId(), gh.getId(), this.setInternalHumidity(actuators, sns.getValue()),
						sns.getStatus(), sns.getName());
			}
			break;
		case "grHum":
			if (sns.getStatus()) {
				dbm.updateSensor(sns.getId(), gh.getId(), this.setGroundHumidity(actuators), sns.getStatus(), sns.getName());
			}
			break;
		case "wind":
			if (sns.getStatus()) {
				dbm.updateSensor(sns.getId(), gh.getId(), extWind, sns.getStatus(), sns.getName());
			}
			break;
		case "light":
			if (sns.getStatus()) {
				dbm.updateSensor(sns.getId(), gh.getId(), extLight, sns.getStatus(), sns.getName());
			}
			break;
		case "extTemp":
			if (sns.getStatus()) {
				dbm.updateSensor(sns.getId(), gh.getId(), extTemp, sns.getStatus(), sns.getName());
			}
			break;
		case "rain":
			if (sns.getStatus()) {
				dbm.updateSensor(sns.getId(), gh.getId(), this.rain, sns.getStatus(), sns.getName());
			}
			break;
		default: // intTemp
			if (sns.getStatus()) {
				dbm.updateSensor(sns.getId(), gh.getId(), this.setInternalTemperature(actuators, sns.getValue()),
						sns.getStatus(), sns.getName());
			}
		}

		paho.publish("monitor/greenhouse/"+ sns.getIdGreenhouse() +"/sensor/" + sns.getType(),
				sns.getId() + "," + sns.getName() + "," + sns.getType() + "," + sns.getValue() + ","
						+ sns.getIdGreenhouse(),
				Constant.simulator_sender);
		
		System.out.println("ORARIO: " + this.hour);
		String time = this.formatTime(clock.get(Calendar.HOUR_OF_DAY), clock.get(Calendar.MINUTE));
		
		paho.publish("openHab/greenhouse/time",
				time,
				Constant.simulator_sender);
	}

	public String formatTime(Integer hour, Integer minutes) {
		
		String h = null;
		String m = null;
		
		if(hour < 10) {
			
			h = "0"+ hour;
		} else {
			h = hour + "";
		}
		
		if (minutes < 1){
			m = minutes + "0";
		} else {
			m = 30 + "";
		}
		
		return h + ":" + m;
	}
	/*
	 * Se piove, umidità aumenta del 10%
	 */
	public double setExternalHumidity() {

		Random rd = new Random();

		// orario compreso tra 22:00 e le 06:00
		if (this.hour >= 22 || this.hour < 7) {
			if (this.hour == 22) {
				this.extHum = rd.nextInt(4) + 60;
			} else {
				this.extHum += 1;
			}
			// orario compreso tra 07:00 e le 13:00
		} else if (this.hour >= 7 && this.hour < 13) {
			if (this.hour == 7) {
				this.extHum = rd.nextInt(4) + 70;
			} else {
				this.extHum -= 0.75;
			}
			// orario compreso tra 13:00 e le 18:00
		} else if (this.hour >= 13 && this.hour < 18) {
			if (this.hour == 13) {
				this.extHum = rd.nextInt(4) + 60;
			} else {
				this.extHum -= 0.5;
			}
			// orario compreso tra 18:00 e le 22:00
		} else if (this.hour >= 18 && this.hour < 22) {
			if (this.hour == 18) {
				this.extHum = rd.nextInt(4) + 55;
			} else {
				this.extHum -= 0.25;
			}
		}
		return this.extHum;
	}

	/*
	 * Umidit� interna varia rispetto a quella esterna
	 * 
	 * Se attuatori 'hum' attivi: (deumidificatore/umidificatore) 1 -> aumenta
	 * l'umidit� del 5%, mezz'ora, 2 -> aumenta l'umidit� del 10%, ogni mezz'ora, 3
	 * -> aumenta l'umidit� del 15%, ogni mezz'ora, -1 -> diminuisce l'umidit� del
	 * 5%, ogni mezz'ora, -2 -> diminuisce l'umidit� del 10%, ogni mezz'ora, -3 ->
	 * diminuisce l'umidit� del 15%, ogni mezz'ora,
	 * 
	 * Se attuatori 'wind' attivi: (finestre) Se umidit� esterna > umidit� interna
	 * aumenta umidit� interna altrimenti diminuisce umidit� interna di un fattore
	 * definito
	 * 
	 * Se attuatori 'wat' attivi: (irrigatori) aumenta l'umidit� del 10%
	 * 
	 */
	public double setInternalHumidity(HashMap<String, Actuator> actuators, double precIntValue) {

		this.intHum = 0;

		if (actuators.get("wind").getStatus()) {
			System.out.println("Sportello attivo");
			if (this.extHum > precIntValue) {
				this.intHum = (this.extHum - precIntValue) / 5;
			} else {
				this.intHum = (this.extHum - precIntValue) / 5;
			}
		}

		this.intHum += (5
				* (precIntValue
						+ (((0 + (actuators.get("hum").getStatus() ? 1 : 0)) * (actuators.get("hum").getPower()))
								+ ((0 + (actuators.get("wat").getStatus() ? 1 : 0)) * (0.1 * precIntValue))))
				+ this.extHum) / 6;

		this.intHum = this.roundingUp(this.intHum, 2);

		/*
		 * l'umidit� non pu� salire oltre il 100%
		 */
		if (this.intHum >= 100) {
			this.intHum = 100;
		}

		return this.intHum;
	}

	/*
	 * Se attuatori 'water' attivi: (irrigatori) aumenta l'umidit� del 10%
	 */
	public double setGroundHumidity(HashMap<String, Actuator> actuators) {

		Random rd = new Random();

		if (this.hour >= 22 || this.hour < 9) {
			if (this.hour == 22) {
				this.grdHum = rd.nextInt(4) + 40;
			} else {
				this.grdHum += 0.5;
			}
		} else if (this.hour >= 9 && this.hour < 14) {
			if (this.hour == 9) {
				this.grdHum = rd.nextInt(4) + 50;
			} else {
				this.grdHum -= 0.75;
			}
		} else if (this.hour >= 14 && this.hour < 19) {
			if (this.hour == 14) {
				this.grdHum = rd.nextInt(4) + 43;
			} else {
				this.grdHum -= 0.5;
			}
		} else if (this.hour >= 19 && this.hour < 22) {
			if (this.hour == 19) {
				this.grdHum = rd.nextInt(4) + 37;
			} else {
				this.grdHum -= 0.25;
			}
		}
		
		this.grdHum += (((0 + (actuators.get("wat").getStatus() ? 1 : 0)) * (0.1 * this.grdHum)));

		this.grdHum = this.roundingUp(this.grdHum, 2);
		
		return this.grdHum;
	}

	public double setExternalTemperature() {

		Random rd = new Random();

		// orario compreso tra 00:00 e le 05:00
		if (this.hour >= 0 && this.hour < 6) {
			if (this.hour == 0) {
				this.extTemp = rd.nextInt(4) + 14;
			} else {
				this.extTemp += 0.25;
			}
			// orario compreso tra 06:00 e le 10:00
		} else if (this.hour >= 6 && this.hour < 11) {
			if (this.hour == 6) {
				this.extTemp = rd.nextInt(4) + 17;
			} else {
				this.extTemp += 0.5;
			}
			// orario compreso tra 11:00 e le 15:00
		} else if (this.hour >= 11 && this.hour < 16) {
			if (this.hour == 11) {
				this.extTemp = rd.nextInt(4) + 22;
			} else {
				this.extTemp += 0.75;
			}
			// orario compreso tra 16:00 e le 19:00
		} else if (this.hour >= 16 && this.hour < 20) {
			if (this.hour == 16) {
				this.extTemp = rd.nextInt(3) + 27;
			} else {
				this.extTemp -= 0.5;
			}
			// orario compreso tra 20:00 e le 23:00
		} else if (this.hour >= 20 && this.hour <= 23) {
			if (this.hour == 20) {
				this.extTemp = rd.nextInt(3) + 25;
			} else {
				this.extTemp -= 0.75;
			}
		}
		return this.extTemp;
	}

	/*
	 * Temperatura interna � uguale a quella esterna ma varia
	 * 
	 * Se attuatori 'light' attivi: (reti ombreggianti) diminuisce di 0.5 grado ogni
	 * mezz'ora
	 * 
	 * A seconda dell'Intensit� Luminosa esterna: 0 -> aumenta 0 gradi ogni
	 * mezz'ora, 1 -> aumenta 1 gradi ogni mezz'ora, 2 -> aumenta 2 grado ogni
	 * mezz'ora, 3 -> aumenta 3 grado ogni mezz'ora
	 * 
	 * se attuatori 'temp' attivi: (condizionatori) se power: 1 -> aumenta di 1
	 * grado ogni mezz'ora, 2 -> aumenta di 2 grado ogni mezz'ora, 3 -> aumenta di 3
	 * grado ogni mezz'ora, -1 -> diminuisce di 1 grado ogni mezz'ora, -2 ->
	 * diminuisce di 2 grado ogni mezz'ora, -3 -> diminuisce di 3 grado ogni
	 * mezz'ora,
	 * 
	 * se attuatori 'wind' attivi: (finestra) se: temperatura interna >
	 * temperaturaesterna se vento: 1 -> diminuisco del 3%, 2 -> diminuisco del 7%
	 * altrimenti: 1 -> aumento del 3%, 2 -> aumento del 7%
	 */
	public double setInternalTemperature(HashMap<String, Actuator> actuators, double precIntValue) {

		double w = 0;

		if (this.extTemp > precIntValue) {
			
			System.out.println("VENTO Esterna: " + this.extWind);
			switch (this.extWind) {
			case 1:
				w = 0.03;
				break;
			case 2:
				w = 0.07;
				break;
			default: // con vento maggiore di 2, le finestre sono chiuse
				break;
			}
		} else {
			switch (this.extWind) {
			case 1:
				w = -0.03;
				break;
			case 2:
				w = -0.07;
				break;
			default: // con vento maggiore di 2, le finestre sono chiuse
				break;
			}
		}

		this.intTemp = 0;

		switch (this.extLight) {
		case 3:
			if (actuators.get("light").getStatus()) {
				this.intTemp += 1;
			} else {
				this.intTemp += 2;
			}
			break;
		case 2:
			if (actuators.get("light").getStatus()) {
				this.intTemp += 0.5;
			} else {
				this.intTemp += 1;
			}
			break;
		case 1:
			if (actuators.get("light").getStatus()) {
				this.intTemp += 0;
			} else {
				this.intTemp += 0.5;
			}
			break;
		default:
			break;
		}
/*
		this.intTemp =
				(7*(precIntValue
						+ (( (0 + (actuators.get("temp").getStatus() ? 1 : 0)) * (actuators.get("temp").getPower()) )
						+ ((0 + (actuators.get("wind").getStatus() ? 1 : 0)) * (w * this.extTemp))))
				+3*this.extTemp) / 10 + 0.8;
*/
		this.intTemp = ((9.5*(precIntValue + ((0 + (actuators.get("wind").getStatus() ? 1 : 0)) * (w * this.extTemp)))
		        + (0.5*this.extTemp))/10);
		
		this.intTemp += ((0 + (actuators.get("temp").getStatus() ? 1 : 0)) * (1.5 * actuators.get("temp").getPower()));
		  
		this.intTemp = this.roundingUp(this.intTemp, 2);
		System.out.println("TEMPERATURA INTERNA: " + this.intTemp);
		return this.intTemp;
	}

	/*
	 * Intensit� del vento identifacata come:
	 * 
	 * 0 -> senza vento, 1 -> vento moderato, 2 -> molto vento
	 */
	public int setWind() {

		int w = new Random().nextInt(11);

		if (w >= 0 && w < 6) {
			// non influisce sulla temperature interna
			this.extWind = 1;
		} else if (w >= 6 && w < 10) {
			// influisce sulla temperatura interna
			this.extWind = 2;
		} else {
			// pericoloso!
			this.extWind = 3;
		}
		return this.extWind;
	}

	/*
	 * Intensit� luminosa identifacata come:
	 * 
	 * 0 -> molto nuvoloso o pioggia, 
	 * 1 -> poco nuvoloso, 
	 * 2 -> soleggiato, 
	 * 3 -> molto soleggiato, quindi molto caldo!
	 */
	public int setLight() {

		Random l = new Random();

		// orario compreso tra 00:00 e le 05:00
		if (this.hour >= 22 || this.hour < 6) {
			this.extLight = 0;
			// orario compreso tra 06:00 e le 10:00
		} else if (this.hour >= 6 && this.hour < 11) {
			this.extLight = l.nextInt(2) + 1;
			// orario compreso tra 11:00 e le 16:00
		} else if (this.hour >= 11 && this.hour < 17) {
			this.extLight = l.nextInt(3) + 1;
			// orario compreso tra 17:00 e le 19:00
		} else if (this.hour >= 17 && this.hour < 20) {
			this.extLight = l.nextInt(2) + 1;
			// orario compreso tra 20:00 e le 23:00
		} else if (this.hour >= 20 && this.hour <= 21) {
			this.extLight = l.nextInt(2);
		}
		return this.extLight;
	}

	/*
	 * Se gerenato un valore: da 0 a 8 -> assenza di pioggia, da 9 a 10 -> presenza
	 * di pioggia
	 */
	public int setRain() {

		int r = new Random().nextInt(11);

		if (r >= 0 && r < 7) {
			// non piove
			this.rain = 0;
		} else if (r >= 8 && r < 11) {
			// piove
			this.rain = 1;
		}
		return this.rain;
	}

	// Arrotondamento per eccesso
	public double roundingUp(double value, int numCifreDecimali) {

		double temp = Math.pow(10, numCifreDecimali);
		return Math.ceil(value * temp) / temp;
	}
}