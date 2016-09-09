package de.l3s.image.tools;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;

public class FeatureCreator {

	public static void main(String[] args) {
		File startdir = new File("/data/zerr/tagpics");
	}

	private int threadsnr;


	public FeatureCreator(int threadsnr) {
		this.threadsnr=threadsnr;
	}
	
	public void gothrough(File startdir, FeatureListener l) {
		LinkedList<File> list = new LinkedList<File>();
		list.add(startdir);

		ThreadedWorker t = new ThreadedWorker(threadsnr);
HashSet<String> usedids=new HashSet<String>();
		while (list.size() > 0) {
			File cur = list.pollLast();
if(!cur.exists())
{
System.err.print("no such File: "+cur);	
}
			if (cur.isDirectory()) {
				for (File f : cur.listFiles()) {
					list.add(f);
				}
			} else {
				
				if(!l.accept(cur)){continue;}
				/*
				if(cur.getAbsoluteFile().toString().endsWith("_s.jpg"))
				{
					continue;
				}
				*/
				
				TWorker worker = t.getWorker();
				
				worker.executeTask(l.getTask(cur));
			}

		}
		
		t.finish();
		
		l.ready();
	}

}
