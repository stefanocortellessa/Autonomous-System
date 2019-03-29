package sg.activator;

import sg.monitor.Monitor;
import sg.mysqldb.DBManager;
import sg.simulator.Simulator;

public class Activator{
	public static void main(String[] args) throws InterruptedException {
		
		//disattivo gli eventuali piani attivi nel database
		DBManager db = new DBManager();
		db.clearActivePlans();
		
		//avvio i thread relativi al simulatore, al monitor
		Simulator simulator = new Simulator();
		Monitor monitor = new Monitor();
		
		simulator.start();
		monitor.start();
	}
}