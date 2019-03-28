package sg.history;

import java.util.Calendar;

public class History {
	private double extTemp;
	private double intTemp;
	private double extHum;
	private double intHum;
	private double wind;
	private double light;
	private double rain;
	private double grHum;
	private int air_vents;
	private int humidifier;
	private int conditioner;
	private int net;
	private int sprinkler;
	private Calendar datetime;
	private int id_greenhouse;
	
	public History(){}
	
	
	public History(double extTemp, double intTemp, double extHum, double intHum, double wind, double light, double rain,
			double grHum, int air_vents, int humidifier, int conditioner, int net, int sprinkler, Calendar datetime,
			int id_greenhouse) {
		this.extTemp = extTemp;
		this.intTemp = intTemp;
		this.extHum = extHum;
		this.intHum = intHum;
		this.wind = wind;
		this.light = light;
		this.rain = rain;
		this.grHum = grHum;
		this.air_vents = air_vents;
		this.humidifier = humidifier;
		this.conditioner = conditioner;
		this.net = net;
		this.sprinkler = sprinkler;
		this.datetime = datetime;
		this.id_greenhouse = id_greenhouse;
	}


	public double getExtTemp() {
		return extTemp;
	}
	public void setExtTemp(double extTemp) {
		this.extTemp = extTemp;
	}
	
	public double getIntTemp() {
		return intTemp;
	}
	public void setIntemp(double intTemp) {
		this.intTemp = intTemp;
	}
	
	public double getExtHum() {
		return extHum;
	}
	public void setExtHum(double extHum) {
		this.extHum = extHum;
	}
	
	public double getIntHum() {
		return intHum;
	}
	public void setIntHum(double intHum) {
		this.intHum = intHum;
	}
	
	public double getWind() {
		return wind;
	}
	public void setWind(double wind) {
		this.wind = wind;
	}
	
	public double getLight() {
		return light;
	}
	public void setLight(double light) {
		this.light = light;
	}
	
	public double getRain() {
		return rain;
	}
	public void setRain(double rain) {
		this.rain = rain;
	}
	
	public double getGrHum() {
		return grHum;
	}
	public void setGrHum(double grHum) {
		this.grHum = grHum;
	}
	
	public int getAir_vents() {
		return air_vents;
	}
	public void setAir_vents(int air_vents) {
		this.air_vents = air_vents;
	}
	
	public int getHumidifier() {
		return humidifier;
	}
	public void setHumidifier(int humidifier) {
		this.humidifier = humidifier;
	}
	
	public int getConditioner() {
		return conditioner;
	}
	public void setConditioner(int conditioner) {
		this.conditioner = conditioner;
	}
	
	public int getNet() {
		return net;
	}
	public void setNet(int net) {
		this.net = net;
	}
	
	public int getSprinkler() {
		return sprinkler;
	}
	public void setSprinkler(int sprinkler) {
		this.sprinkler = sprinkler;
	}
	
	public Calendar getDatetime() {
		return datetime;
	}
	public void setDatetime(Calendar datetime) {
		this.datetime = datetime;
	}
	
	public int getId_greenhouse() {
		return id_greenhouse;
	}
	public void setId_greenhouse(int id_greenhouse) {
		this.id_greenhouse = id_greenhouse;
	}
	
}
