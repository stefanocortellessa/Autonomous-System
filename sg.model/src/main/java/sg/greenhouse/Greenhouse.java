package sg.greenhouse;

public class Greenhouse{
	
	private int id;
	private String name;
	private String plant;
	private int opt_temp;
	private int opt_hum;
	private int opt_light;
	private int opt_t_hum;
	private int range_temp;
	private int range_hum;
	private int range_t_hum;
	
	public Greenhouse(){}
	
	public Greenhouse(int id, String name, String plant, int opt_temp, int opt_hum, int opt_light, int opt_t_hum, 
			int range_temp, int range_hum, int range_t_hum){
		
		this.id = id;
		this.name = name;
		this.plant = plant;
		this.opt_temp = opt_temp;
		this.opt_hum = opt_hum;
		this.opt_light = opt_light;
		this.opt_t_hum = opt_t_hum;
		this.range_temp = range_temp;
		this.range_hum = range_hum;
 		this.range_t_hum = range_t_hum;		
	}
	
	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPlant() {
		return this.plant;
	}
	public void setPlant(String plant) {
		this.plant = plant;
	}
	
	public int getOpt_temp() {
		return this.opt_temp;
	}
	public void setOpt_temp(int opt_temp) {
		this.opt_temp = opt_temp;
	}
	
	public int getOpt_hum() {
		return this.opt_hum;
	}
	public void setOpt_hum(int opt_hum) {
		this.opt_hum = opt_hum;
	}
	
	public int getOpt_light() {
		return this.opt_light;
	}
	public void setOpt_light(int opt_light) {
		this.opt_light = opt_light;
	}
	
	public int getOpt_terrain_hum() {
		return this.opt_t_hum;
	}
	
	public void setOpt_terrain_hum(int opt_t_hum){
		this.opt_t_hum = opt_t_hum;
	}		
	
	public int getRange_temp() {
		return this.range_temp;
	}
	public void setRange_temp(int range_temp) {
		this.range_temp = range_temp;
	}
	
	public int getRange_hum() {
		return this.range_hum;
	}
	public void setRange_hum(int range_hum) {
		this.range_hum = range_hum;
	}
	
	public int getRange_terrain_hum() {
		return this.range_t_hum;
	}
	public void setRange_terrain_hum(int range_hum){
		this.range_t_hum = range_hum;
	}
}

