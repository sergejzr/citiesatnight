package de.l3s.image.tools;
import java.util.Vector;


public class ThreadedWorker {

	Vector<TWorker> tasks=new Vector<TWorker>();
	private int maxthreads;
	
	public ThreadedWorker(int maxthreads)
	{
		this.maxthreads=maxthreads;
	}
	
	
	public TWorker getWorker()
	{
		if(tasks.size()<maxthreads)
		{
			TWorker t;
			tasks.add(t=new TWorker());
			t.start();
			return t;
		}
		
		for(TWorker t:tasks)
		{
			if(t.isReady())
			{
				return t;
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getWorker();
	}


	public synchronized void finish() {

	
			boolean ready;
			do{
				ready=true;
			for(TWorker t:tasks)
			{
				if(!t.isReady())
				{ ready=false;
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
				t.shutDown();
			}
			}while(!ready);
			
			for(TWorker t:tasks)
			{
				if(!t.isReady())
				{
					System.err.println("Some are not ready yet");
				}
			}
	}
}
