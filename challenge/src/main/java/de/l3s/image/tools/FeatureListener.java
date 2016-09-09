package de.l3s.image.tools;
import java.io.File;
import java.util.Hashtable;

import javax.sql.rowset.spi.SyncResolver;


public interface FeatureListener {

	public  void featuresRead(String id, Hashtable<String, String> extractFrom);
	public void ready();
	public boolean accept(File cur);
	public TTask getTask(File cur);
}
