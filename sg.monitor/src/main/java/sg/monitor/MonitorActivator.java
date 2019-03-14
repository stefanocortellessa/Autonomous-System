package sg.monitor;

import sg.paho.PahoCommunicator;

public class MonitorActivator{
	public static void main(String[] args) throws InterruptedException {
			Monitor monitor = new Monitor();
			monitor.start();
						
		}
	}