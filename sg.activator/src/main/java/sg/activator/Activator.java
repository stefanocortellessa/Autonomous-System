package sg.activator;

import sg.executor.Executor;
import sg.monitor.Monitor;
import sg.simulator.Simulator;

public class Activator{
	public static void main(String[] args) throws InterruptedException {
			
		Simulator simulator = new Simulator();
		Monitor monitor = new Monitor();
		Executor executor = new Executor();
		
		simulator.start();
		monitor.start();
		executor.start();
	}
}