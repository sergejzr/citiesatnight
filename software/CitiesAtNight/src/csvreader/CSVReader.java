package csvreader;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import db.DB;
import de.l3s.util.image.FileDistributer;

public class CSVReader {

	public static void main(String[] args) {
		CSVReader reader=new CSVReader();
	try {
		reader.read();
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}

	private void read() throws ClassNotFoundException, SQLException {
		File csvdir=new File("/media/zerr/BA0E0E3E0E0DF3E3/darkskies/");
	
		
		 ArrayBlockingQueue<Runnable> queue;
		ThreadPoolExecutor executorPool = new ThreadPoolExecutor(4, 6, 100, TimeUnit.MINUTES, queue=new ArrayBlockingQueue<Runnable>(100));

		
		
		CSVFormat format = CSVFormat.EXCEL.withHeader().withDelimiter(',').withQuote('"');
		
		
		
		
		

	
	Connection con = DB.getLocalConnection();

			InputStreamReader ir;
			try {
				ir = new InputStreamReader(new FileInputStream(new File(csvdir,"darkskies_task_run.csv")),
						StandardCharsets.UTF_8);
				CSVParser parser = new CSVParser(ir, format);
				List<CSVRecord> towork=new ArrayList<>();				

				for (CSVRecord record : parser) {
					
					if(towork.size()>100)
					{
						while(queue.size()>90)
						{
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						executorPool.execute(new ISSImageworker(towork,con));
						towork.clear();
					}
					
				towork.add(record);
	
					
				}
				if(towork.size()>0)
				{
					executorPool.execute(new ISSImageworker(towork,con));
				}
			parser.close();
			
			try {
				executorPool.awaitTermination(1, TimeUnit.HOURS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		
	}
}
