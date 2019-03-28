package sg.plan;

import java.util.Calendar;

public class Plan {
	
	private int id;
	private String type;
	private String mode;
	private boolean active;
	private double trigger_value;
	private int problem_code;
	private Calendar start_date;
	private Calendar end_date;
	private int idGreenhouse;
	
	public Plan(){}

	
	
	public Plan(int id, String type, String mode, boolean active, double trigger_value, int problem_code, Calendar start_date,
			Calendar end_date, int idGreenhouse) {
		this.id = id;
		this.type = type;
		this.mode = mode;
		this.active = active;
		this.trigger_value = trigger_value;
		this.problem_code = problem_code;
		this.start_date = start_date;
		this.end_date = end_date;
		this.idGreenhouse = idGreenhouse;
	}



	public int getId(){
		return this.id;
	}

	public void setId(int id){
		this.id = id;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setType(String type){
		this.type =type;
	}

	public boolean getActive(){
		return this.active;
	}
	
	public void setActive(boolean active){
		this.active = active;
	}
	

	public int getProblemCode(){
		return this.problem_code;
	}
	
	public void setProblemCode(int problem_code){
		this.problem_code = problem_code;
	}
	
	public int getIdGreenhouse() {
		return idGreenhouse;
	}
	public void setIdGreenhouse(int idGreenhouse) {
		this.idGreenhouse = idGreenhouse;
	}

	public Calendar getStart_date() {
		return start_date;
	}

	public void setStart_date(Calendar start_date) {
		this.start_date = start_date;
	}

	public Calendar getEnd_date() {
		return end_date;
	}

	public void setEnd_date(Calendar end_date) {
		this.end_date = end_date;
	}



	public double getTrigger_value() {
		return trigger_value;
	}



	public void setTrigger_value(double trigger_value) {
		this.trigger_value = trigger_value;
	}



	public String getMode() {
		return mode;
	}



	public void setMode(String mode) {
		this.mode = mode;
	}
	
}	
