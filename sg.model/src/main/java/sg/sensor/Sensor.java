package sg.sensor;

public class Sensor {
	
	private int id;
	private int idGreenhouse;
	private double value;
	private String type;
	private String name;
	private boolean status;
	
	public Sensor(){}
	
	public Sensor(int id, String type, String name,  double val, int idGreenhouse) {
		this.id= id;
		this.type = type;
		this.name = name;
		this.value = val;
		this.idGreenhouse = idGreenhouse;
		this.status = true;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIdGreenhouse() {
		return idGreenhouse;
	}
	public void setIdGreenhouse(int idGreenhouse) {
		this.idGreenhouse = idGreenhouse;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean getStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
}