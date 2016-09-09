package de.l3s.image.tools;
public class TWorker extends Thread {

	private boolean isready;
	private TTask tTask;

	public void execute() {
		isready = false;
		start();

	}
	boolean shutdown=false;
	@Override
	public void run() {
		super.run();
		
		workrun:
		while (!shutdown) {
			
			while(tTask==null)
			{
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue workrun;
			}
			
			tTask.execute();
			tTask=null;
			isready = true;
			
		}

	}

	public boolean isReady() {
		return isready;
	}

	public synchronized void executeTask(TTask tTask) {
		
		isready = false;
		this.tTask=tTask;
		
	}

	public synchronized void shutDown() {
		this.shutdown=true;
		
	}

}
