package sg.actuator;

public class Actuator {
	
	private int id;
	private int power;
	private String type;
	private String name;
	private boolean status;
	private int idGreenhouse;
	
	public Actuator(){}
	
	public Actuator(int power,String type){
		this.power = power;
		this.type = type;
	}
	
	public Actuator(int id, int power, String type, String name, boolean status, int idGreenhouse){
		this.id = id;
		this.power = power;
		this.type = type;
		this.name = name;
		this.status = status;
		this.idGreenhouse = idGreenhouse;
	}
	
	
	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public int getPower() {
		return this.power;
	}
	public void setPower(int value) {
		this.power = value;
	}
	
	public String getType() {
		return this.type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean getStatus() {
		return this.status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	public int getIdGreenhouse() {
		return this.idGreenhouse;
	}
	public void setIdGreenhouse(int idGreenhouse) {
		this.idGreenhouse = idGreenhouse;
	}
}