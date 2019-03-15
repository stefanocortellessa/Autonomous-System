package sg.activator;

import sg.monitor.Monitor;
import sg.simulator.Simulator;
import sg.updater.Updater;

public class Activator{
	public static void main(String[] args) throws InterruptedException {
			
		Simulator simulator = new Simulator();
		Monitor monitor = new Monitor();
		Updater executor = new Updater();
		
		simulator.start();
		monitor.start();
		executor.start();
	}
}