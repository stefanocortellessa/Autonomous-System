package sg.simulator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import sg.paho.PahoCommunicator;
import sg.greenhouse.Greenhouse;
import sg.mysqldb.DBManager;
import sg.sensor.Sensor;
import sg.actuator.Actuator;
import sg.constant.Constant;

public class Simulator extends Thread{

	private Calendar clock = Calendar.getInstance();
	private PahoCommunicator paho_mape = new PahoCommunicator();
	private PahoCommunicator paho_updater = new PahoCommunicator();
	private Map<Integer,HashMap<String,Sensor>> sensors = new HashMap<Integer,HashMap<String,Sensor>>();
	private Map<Integer,HashMap<String,Actuator>> actuators = new HashMap<Integer,HashMap<String,Actuator>>();
	private DBManager dbm = new DBManager();
	private double hour = 0;
	private double extTemp;
	private double extHum;
	private double extLight;
	private double extWind;
	private double rain = 0.0;

	
	public Simulator(){
		
		// sottoscrizione al canale del componente Executor
		this.paho_mape.subscribe(Constant.executor_channel);
		
		// sottoscrizione al canale del componente Updater
		this.paho_updater.subscribe(Constant.updater_channel);
		
		//inizializzo il clock
		this.clock.set(Calendar.HOUR_OF_DAY, 0);
		this.clock.set(Calendar.MINUTE, 0);
		
		/* inizialmente recupero dal DB tutti i sensori e gli attuatori a disposizione con i relativi valori
		e creo le due mappe 'sensors' e 'actuators' per gestirli in maniera appropriata.*/
		ArrayList<Greenhouse> greenhouses = this.dbm.selectAllGreenhouses();
		for (Greenhouse greenhouse: greenhouses){
			this.sensors.put(greenhouse.getId(), dbm.selectGreenhouseSensors(greenhouse.getId()));	
			this.actuators.put(greenhouse.getId(), dbm.selectGreenhouseActuators(greenhouse.getId()));	
		}	
		
	}
	
	public void simulationUpdate(){
		ArrayList<Greenhouse> greenhouses = this.dbm.selectAllGreenhouses();
		for (Greenhouse greenhouse: greenhouses){
			this.sensors.put(greenhouse.getId(), dbm.selectGreenhouseSensors(greenhouse.getId()));	
			this.actuators.put(greenhouse.getId(), dbm.selectGreenhouseActuators(greenhouse.getId()));	
		}	
	}
	
	public void run() {
		
		
		
		while (true) {
			try {
				// il metodo simulation si occupa di effettuare la simulazione a intervalli di 5 secondi
				this.simulation();				
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void simulation() {
		
		// aggiungiamo 30 minuti ad ogni chiamata di funzione
		System.out.println("------------");
		System.out.println("CLOCK: " +this.formatTime(this.clock.get(Calendar.HOUR_OF_DAY) , this.clock.get(Calendar.MINUTE)));
		System.out.println("------------");
		this.hour = clock.get(Calendar.HOUR_OF_DAY);

		/*	 inizialmente chiamiamo i metodi di settaggio dei fattori atmosferici esterni, uguali per tutte le serre; 
		 *   in particolare settiamo il livello di luce esterna, la temperatura, l'umidità, il vento e la pioggia. */	
		if (this.hour == 0 || this.hour == 6 || this.hour == 12 || this.hour == 18 ){
			this.rain = this.setRain();
		}
		this.extLight = this.setLight();
		this.extTemp = this.setExternalTemperature();
		this.extHum = this.setExternalHumidity();
		this.extWind = this.setWind();
		

		// recupero tutti i dati relativi agli attuatori che mi arrivano tramite mqtt dall'executor.
		for(Map.Entry<Integer, String> message : this.paho_mape.getMessages().entrySet()){
			
			this.actuators.get(Constant.get_id_greenhouse(this.paho_mape.getTopics().get(message.getKey())))
					.get(Constant.get_actuator_type(this.paho_mape.getTopics().get(message.getKey())))
					.setPower(Integer.parseInt(message.getValue()));
		}
		
		//svuoto la mappa dei messaggi
		this.paho_mape.getMessages().clear();
		
		// recupero tutti i dati relativi agli attuatori che mi arrivano tramite mqtt dall'updater.		
		for(Map.Entry<Integer, String> message : this.paho_updater.getMessages().entrySet()){
			this.actuators.get(Constant.get_id_greenhouse(this.paho_updater.getTopics().get(message.getKey())))
					.get(Constant.get_actuator_type(this.paho_updater.getTopics().get(message.getKey())))
					.setPower(Integer.parseInt(message.getValue()));
		}
		//svuoto la mappa dei messaggi
		this.paho_updater.getMessages().clear();
		
		/*  una volta raccolti tutti i dati degli attuatori con i rispettivi stati e valori posso chiamare i metodi 'updateSensorValues'
		 *  e 'actuatorsToMonitor'. Il primo, tenendo conto dei dati ricevuti, andrà a generare le opportune simulazioni per i sensori
		 *  instradandole verso il monitor. Il secondo andrà a pubblicare sempre verso il monitor tutte le informazioni degli attuatori.
		 */			
		for (Map.Entry<Integer, HashMap<String,Sensor>> gh : this.sensors.entrySet()) {
			this.updateSensorValues(gh.getKey(),actuators.get(gh.getKey()));	
			this.actuatorsToMonitor(gh.getKey());
		}
		
		//il publish seguente ci permette di visualizzare l'ora corrente all'interno di openhab
		String time = this.formatTime(clock.get(Calendar.HOUR_OF_DAY), clock.get(Calendar.MINUTE));
		  
		this.paho_updater.publish(Constant.clock_channel, time);
		this.clock.add(Calendar.MINUTE, 30);
		
		//una volta al giorno, a mezzanotte controllo l'eventuale aggiunta di nuove serre
		if (this.clock.get(Calendar.HOUR) == 0 && this.clock.get(Calendar.MINUTE)==0){
			this.simulationUpdate();
		}
	}

	public void actuatorsToMonitor(int idGh){
		
		for (Map.Entry<String, Actuator> act : this.actuators.get(idGh).entrySet() ){		
			this.paho_mape.publish(Constant.monitor_actuator_channel.replace("+", Integer.toString(act.getValue().getIdGreenhouse())).replace("#", act.getValue().getType()),
					act.getValue().getId() + Constant.sep + act.getValue().getName() + Constant.sep + 
					act.getValue().getType() + Constant.sep + act.getValue().getPower() + Constant.sep + act.getValue().getIdGreenhouse());		
		}	
	}	
	
	// come descritto in precedenza questo metodo andrà a chiamare per ogni tipo di sensore l'opportuna funzione di simulazione	
	public void updateSensorValues(Integer idGh, HashMap<String, Actuator> actuators) {

		if(this.sensors.get(idGh).get(Constant.ext_hum_type) != null)
		{
			this.sensors.get(idGh).get(Constant.ext_hum_type).setValue(this.extHum);
		}
		
		if(this.sensors.get(idGh).get(Constant.int_hum_type) != null)
		{
			this.sensors.get(idGh).get(Constant.int_hum_type).setValue(this.setInternalHumidity(actuators, 
				this.sensors.get(idGh).get(Constant.int_hum_type).getValue()));
		}
		
		if(this.sensors.get(idGh).get(Constant.terrain_hum_type) != null)
		{
			this.sensors.get(idGh).get(Constant.terrain_hum_type).setValue(this.setGroundHumidity(actuators,
				this.sensors.get(idGh).get(Constant.terrain_hum_type).getValue()));
		}
		
		if(this.sensors.get(idGh).get(Constant.wind_type) != null)
		{
			this.sensors.get(idGh).get(Constant.wind_type).setValue(this.extWind);
		}
		
		if(this.sensors.get(idGh).get(Constant.light_type) != null)
		{
			this.sensors.get(idGh).get(Constant.light_type).setValue(this.extLight);
		}
		
		if(this.sensors.get(idGh).get(Constant.ext_temp_type) != null)
		{
			this.sensors.get(idGh).get(Constant.ext_temp_type).setValue(this.extTemp);
		}
		
		if(this.sensors.get(idGh).get(Constant.rain_type) != null)
		{
			this.sensors.get(idGh).get(Constant.rain_type).setValue(this.rain);
		}
		
		if(this.sensors.get(idGh).get(Constant.int_temp_type) != null)
		{
			this.sensors.get(idGh).get(Constant.int_temp_type).setValue(this.setInternalTemperature(actuators, 
				this.sensors.get(idGh).get(Constant.int_temp_type).getValue()));
		}
		
		for (Map.Entry<String, Sensor> sns : this.sensors.get(idGh).entrySet() ){		
			this.paho_mape.publish(Constant.monitor_sensor_channel.replace("+", Integer.toString(sns.getValue().getIdGreenhouse())).replace("#", sns.getValue().getType()),
					sns.getValue().getId() + Constant.sep + sns.getValue().getName() + Constant.sep + 
					sns.getValue().getType() + Constant.sep + sns.getValue().getValue() + Constant.sep + sns.getValue().getIdGreenhouse());
			

		}
		
	}
	
	// questo metodo ci permette di formattare in modo corretto l'ora corrente
	public String formatTime(Integer hour, Integer minutes) {
		
		String time = new String();
		
		if(hour < 10) {
			
			time = "0"+ hour;
		} else {
			time = Integer.toString(hour);
		}
		
		if (minutes < 10){
			time =time.concat(":0"+minutes);
		} else {
			time = time.concat(":"+minutes);
		}
		
		return time;
	}
	
	
	/* Anche la temperatura esterna è settata in base alle fasce orarie, seguendo una curva simile a quella reale.
	 * Essa tende a raggiungere il picco durante le prime ore pomeridiane via via abbassandosi in modo quasi 
	 * costante durante le ore serali.
	 * 
	 * Il valore minimo di temperatura esterna si verifica durante le ore notturne.
	 */
	public double setExternalTemperature() {

		Random rd = new Random();
		// orario compreso tra 00:00 e le 05:00
		if (this.hour >= 0 && this.hour < 6) {
			if (this.hour == 0.0) {
				this.extTemp = rd.nextInt(4) + 14;
				
			} else {
				this.extTemp += 0.25;
			}
			// orario compreso tra le 06:00 e le 10:00
		} else if (this.hour >= 6 && this.hour < 11) {
			if (this.hour == 6) {
				this.extTemp = rd.nextInt(4) + 17;
			} else {
				this.extTemp += 0.5;
			}
			// orario compreso tra le 11:00 e le 15:00
		} else if (this.hour >= 11 && this.hour < 16) {
			if (this.hour == 11) {
				this.extTemp = rd.nextInt(4) + 22;
			} else {
				this.extTemp += 0.75;
			}
			// orario compreso tra le 16:00 e le 19:00
		} else if (this.hour >= 16 && this.hour < 20) {
			if (this.hour == 16) {
				this.extTemp = rd.nextInt(3) + 27;
			} else {
				this.extTemp -= 0.5;
			}
			// orario compreso tra le 20:00 e le 23:00
		} else if (this.hour >= 20 && this.hour <= 23) {
			if (this.hour == 20) {
				this.extTemp = rd.nextInt(3) + 25;
			} else {
				this.extTemp -= 0.75;
			}
		}
		
		return this.extTemp;
	}
	
	
	/* l'umidità esterna è settata in base alle fasce orarie, seguendo una curva simile a quella reale.
	 * Essa tende a raggiungere il picco in mattinata via via abbassandosi in modo quasi costante durante le ore 
	 * del giorno e risalire di nuovo durante la notte. 
	 */
	public double setExternalHumidity() {

		Random rd = new Random();

			// orario compreso tra le 00:00 e le 05:00
		if (this.hour >= 0 && this.hour < 6) {
			if (this.hour == 0) {
				this.extHum = rd.nextInt(2) + 60;
			} else {
				this.extHum += 1;
			}
			// orario compreso tra le 06:00 e le 10:00
		} else if (this.hour >= 6 && this.hour < 11) {
			if (this.hour == 6) {
				this.extHum = rd.nextInt(2) + 70;
			} else {
				this.extHum -= 0.75;
			}
			// orario compreso tra le 11:00 e le 15:00
		} else if (this.hour >= 11 && this.hour < 16) {
			if (this.hour == 11) {
				this.extHum = rd.nextInt(2) + 63;
			} else {
				this.extHum -= 0.75;
			}
			// orario compreso tra le 16:00 e le 19:00
		} else if (this.hour >= 16 && this.hour < 20) {
			if (this.hour == 16) {
				this.extHum = rd.nextInt(2) + 56;
			} else {
				this.extHum -= 0.25;
			}
			// orario compreso tra le 20:00 e le 23:00
		} else if (this.hour >= 20 && this.hour <= 23) {
			if (this.hour == 18) {
				this.extHum = rd.nextInt(2) + 54;
			} else {
				this.extHum += 0.75;
			}
		}
				
		if (this.extHum < 0) this.extHum = 0;
		else if (this.extHum > 100) this.extHum = 100;
				
		return this.extHum;
	}

	/* la seguente funzione si occupa di generare un valore realistico per l'umidità interna
	 * prendendo in considerazione dati legati sia a sensori che attuatori.
	 * I fattori che influiscono sull'umidità interna sono:
	 * 
	 * - l'umidità esterna
	 * - il deumidificatore, in base alla potenza settata
	 * - gli irrigatori
	 * - il vento nel caso in cui gli sportelli fossero aperti 
	 */
	public double setInternalHumidity(HashMap<String, Actuator> actuators, double precIntValue) {

		double intHum = 0;
	 // se gli sportelli sono aperti la temperatura interna tende ad adattarsi a quella esterna più velocemente 
		if (actuators.get(Constant.air_vents).getPower() == 1) {			
			intHum = (this.extHum - precIntValue) / 5;
		}		
	/* se sono attivi gli irrigatori l'umidità interna tende ad aumentare di circa il 3% rispetto al valore precedente;
	 * viene inoltre effettuata una media pesata tenendo in considerazione l'umidità esterna.
	 * Nel caso in cui i deumidificatori fossero attivi andranno ad influire sull'umidità interna in base alla potenza settata.
	 */		
		if (actuators.get(Constant.humidifier).getPower() == 0)
		{	intHum += ((9 * (precIntValue + actuators.get(Constant.sprinkler).getPower()/2)	+ (1 * this.extHum)) / 10);
		}		
		else
		{	intHum += (precIntValue + actuators.get(Constant.sprinkler).getPower()/2) +
					  (actuators.get(Constant.humidifier).getPower() != 0 ? 1 : 0) * (0.98 * actuators.get(Constant.humidifier).getPower());
		}

		
		intHum = this.roundingUp(intHum, 2);
	 // il limite massimo per l'umidità interna è del 100%

		if (intHum >= 100) {
			intHum = 100;
		}

		return intHum;
	}

	/* la seguente funzione si occupa di generare un valore per l'umidità del terreno
	 * prendendo in considerazione i dati legati all'attuatore irrigatore.
	 * l'umidità del terreno tende a diminuire nell'arco della giornata fino alla prossima
	 * attivazione degli irrigatori.
	 * 
	 * all'attivazione di questi ultimi abbiamo un incremento dell'umidità del terreno del
	 * 50% rispetto al valore attuale.
	 * 
	 */
	public double setGroundHumidity(HashMap<String, Actuator> actuators, double precGrdValue) {

		double grdHum = precGrdValue;
		
		if(actuators.get(Constant.sprinkler).getPower() == 1) {			
			grdHum += (((0 + (actuators.get(Constant.sprinkler).getPower())) * (0.5 * grdHum)));			
		}
		else {
			grdHum -= (0.02*grdHum);
		}
		
		if (grdHum >= 100) {
			grdHum = 100;
		}
		grdHum = this.roundingUp(grdHum, 2);
		return grdHum;
	}

	/* la seguente funzione si occupa di generare un valore realistico per la temperatura interna
	 * prendendo in considerazione dati legati sia a sensori che attuatori.
	 * I fattori che influiscono sulla temperatura interna sono:
	 * 
	 * - la temperatura esterna
	 * - l'intensità luminosa
	 * - il condizionatore, in base alla potenza settata
	 * - le reti ombreggianti
	 * - il vento nel caso in cui gli sportelli fossero aperti 
	 */
	public double setInternalTemperature(HashMap<String, Actuator> actuators, double precIntValue) {
		
		double intTemp = 0;
		double w = 0;

	/* se gli sportelli sono aperti la temperatura interna tende ad adattarsi a quella 
	 * esterna più velocemente. Un altro fattore che influenza ciò è il vento, infatti
	 * un vento con forza 2 influisce in maniera più evidente rispetto a una forza pari a 1.
	 */
		if (this.extTemp > precIntValue) {			
			if (this.extWind == 1.0){
				w = 0.03;
			}else if(this.extWind == 2.0){
				w = 0.07;
			}		
		} else {
			if (this.extWind == 1.0){
				w = - 0.03;
			}else if(this.extWind == 2.0){
				w =  - 0.07;
			}
		}
		
		/* Altri due importanti fattori che influiscono sulla temperatura interna alla serra sono
		 * l'intensità luminosa e le reti ombreggianti.
		 * 
		 * Più è elevata l'intensità luminosa più la temperatura interna tende a salire; le reti 
		 * ombreggianti, se attive, permettono di diminuire l'effettiva intensità che raggiungerà
		 * l'interno della serra.		 * 
		 */
		if (this.extLight == 3.0){
			if (actuators.get(Constant.net).getPower() == 1) {
				intTemp += 0.75;
			} else {
				intTemp += 1;
			}
		}else if(this.extLight == 2.0){
			if (actuators.get(Constant.net).getPower() == 1) {
				intTemp += 0.5;
			} else {
				intTemp += 0.75;
			}
			
		}else if(this.extLight == 1.0){
			if (actuators.get(Constant.net).getPower() == 1) {
				intTemp += 0;
			} else {
				intTemp += 0.25;
			}			
		}
		
		/* se sono attivi gli sportelli la temperatura interna tende a variare di un fattore w rispetto
		  * al valore precedente; viene inoltre effettuata una media pesata tenendo in considerazione la 
		  * temperatura esterna.
		  * Nel caso in cui i condizionatori fossero attivi andranno ad influire sulla temperatura interna in relazione 
		  * alla potenza settata.
		  */
		
		if(actuators.get(Constant.conditioner).getPower() == 0) {			
			intTemp += ((9*(precIntValue + ((0 + (actuators.get(Constant.air_vents).getPower())) * (w * this.extTemp)))
			        + (1*this.extTemp))/10) + 0.4;
		}		
		else {
			intTemp += (precIntValue + ((0 + (actuators.get(Constant.air_vents).getPower())) * (w * this.extTemp))) +
					((0 + (actuators.get(Constant.conditioner).getPower() != 0 ? 1 : 0)) * (0.98 * actuators.get(Constant.conditioner).getPower()));
		}		  
		intTemp = this.roundingUp(intTemp, 2);		
		return intTemp;
	}

	/* la seguente funzione si occupa di generare un valore per il vento esterno.
	 * I valori assumibili dal vento sono 3 e sono generati in base a una probabilità
	 * legata ad ognuno di essi:
	 * 
	 *  - 1 : vento debole (50%)
	 *  - 2 : vento moderato (40%)
	 *  - 3 : vento pericoloso (10%) 
	 */
	public double setWind() {

		int w = new Random().nextInt(11);

		if (w >= 0 && w < 6) {
			this.extWind = 1.0;
		} else if (w >= 6 && w < 10) {
			this.extWind = 2.0;
		} else {
			this.extWind = 3.0;
		}
		return this.extWind;
	}

	/* la seguente funzione si occupa di generare un valore per l'intensità luminosa.
	 * I valori assumibili da questo fattore sono 3 e sono generati in base alla fascia
	 * oraria.
	 * 
	 * l'intensità luminosa minima è 0 e si ha durante la notte, quella massima è 3 e 
	 * mediamente si presenta nel primo pomeriggio.
	 * 
	 * In caso di pioggia, quindi di nuvolosità, l'intensità luminosa descesce di un fattore pari a 1.
	 * 
	 */
	public double setLight() {

		Random l = new Random();

		// orario compreso tra 00:00 e le 05:00
		if (this.hour >= 22 || this.hour < 6) {
			this.extLight = 0;
			// orario compreso tra le 06:00 e le 10:00
		} else if (this.hour >= 6 && this.hour < 11) {
			this.extLight = l.nextInt(2) + 1;
			// orario compreso tra le 11:00 e le 16:00
		} else if (this.hour >= 11 && this.hour < 17) {
			this.extLight = l.nextInt(3) + 1;
			// orario compreso tra le 17:00 e le 19:00
		} else if (this.hour >= 17 && this.hour < 20) {
			this.extLight = l.nextInt(2) + 1;
			// orario compreso tra le 20:00 e le 23:00
		} else if (this.hour >= 20 && this.hour <= 21) {
			this.extLight = l.nextInt(2);
		}
		if (this.extLight != 0)
		this.extLight -= this.rain;		
		
		return new Double(this.extLight);
	}

	/* la seguente funzione si occupa di generare un valore per la pioggia.
	 * I valori assumibili da essa sono generati in base a una probabilità.
	 * 
	 * abbiamo considerato una probabilità media di precipitazione del 30% 
	 */
	public double setRain() {

		int r = new Random().nextInt(11);

		if (r >= 0 && r < 8) {
			this.rain = 0;
		} else if (r >= 8 && r < 11) {
			this.rain = 1;
		}
		return new Double(this.rain);
	}

	/* la seguente funzione viene usata per effettuare un arrotondamento dei valori.
	*/
	public double roundingUp(double value, int numCifreDecimali) {

		double temp = Math.pow(10, numCifreDecimali);
		return Math.ceil(value * temp) / temp;
	}
}