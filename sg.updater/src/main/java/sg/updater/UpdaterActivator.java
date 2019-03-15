package sg.updater;

public class UpdaterActivator{
	public static void main(String[] args) throws InterruptedException {
			
		Updater executor = new Updater();
		
		executor.start();			
		}
	}