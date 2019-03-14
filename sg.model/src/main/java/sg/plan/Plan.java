package sg.plan;
public class Plan {
	
	private int id;
	private String type;
	private boolean active;
	private double current_value;
	private int problem_code;
	private int score;
	private int idGreenhouse;
	
	public Plan(){}
	
	public Plan(int id, String type, boolean active, double current_value, int problem_code,int score, int idGreenhouse){
		this.id = id;
		this.type = type;
		this.active = active;
		this.current_value = current_value;
		this.problem_code = problem_code;
		this.score = score;
		this.idGreenhouse = idGreenhouse;
	}
	
	public Plan(String type, boolean active,double current_value, int problem_code,int idGreenhouse){
		this.type = type;
		this.active = active;
		this.current_value = current_value;
		this.problem_code = problem_code;
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
	
	
	public int getScore(){
		return this.score;
	}
	
	public void setScore(int score){
		this.score= score;
	}
	
	public double getCurrentValue(){
		return this.current_value;
	}
	
	public void setCurrentValue(double current_value){
		this.current_value = current_value;
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
	
}	
