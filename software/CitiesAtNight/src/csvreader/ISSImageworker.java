package csvreader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.csv.CSVRecord;

import db.DB;
import de.l3s.util.encoding.MD5;
import de.l3s.util.image.FileDistributer;

public class ISSImageworker implements Runnable {

	private List<CSVRecord> towork;
	private Connection con;

	public ISSImageworker(List<CSVRecord> towork, Connection con) {
		this.towork = new ArrayList<>();
		this.towork.addAll(towork);
		this.con = con;
	}

	@Override
	public void run() {
		
		PreparedStatement pst=null;		
try{

	 pst = con.prepareStatement("INSERT INTO citiesatnight.darkskies "
			+ "(id, task_run__task_id,task_run__user_id,img_id,sharp,classification,cloudy,img_big,img_small,task_runinfo__cloudy,task_runinfo__sharp) "
			+ "VALUES " + "(NULL,?,?,?,?,?,?,?,?,?,?)");

	for (CSVRecord record : towork) {
		String task_run__task_id;
		String task_run__user_id;
		String task_runinfo__classification;
		String task_runinfo__cloudy;
		String task_runinfo__sharp;
		String task_runinfo__img_small;
		try {
			task_run__task_id = record.get("task_run__id");
			task_run__user_id = record.get("task_run__user_id");
			task_runinfo__classification = record.get("task_run__info");
			//task_runinfo__cloudy = record.get("task_runinfo__cloudy");
			//task_runinfo__sharp = record.get("task_runinfo__sharp");
			task_runinfo__img_small = record.get("task_runinfo__img_small").replace("/sseop/images",
					"/DatabaseImages");
		} catch (Exception e) {
			e.printStackTrace();
			continue;
		}

		task_runinfo__classification = task_runinfo__classification.replaceAll("\"", "").replaceAll("\\{", "")
				.replaceAll("\\}", "");

		Hashtable<String, String> props = new Hashtable<>();
		for (String pairstr : task_runinfo__classification.split(",")) {
			int firstcolonidx = pairstr.indexOf(':');
			// String[] pairarr = pairstr.split(":");
			// props.put(pairarr[0].trim(), pairarr[1].trim());
			props.put(pairstr.substring(0, firstcolonidx).trim(), pairstr.substring(firstcolonidx + 1).trim());
		}
		
		int startpos=task_runinfo__img_small.lastIndexOf('/');
		int endpos=task_runinfo__img_small.lastIndexOf('.');
		String imgfname=task_runinfo__img_small.substring(startpos+1, endpos);
		imgfname=imgfname.replace("-E--E-", "-E-");
		String id = imgfname;
		pst.setString(1, task_run__task_id);
		pst.setString(2, task_run__user_id);
		pst.setString(3, id + "");
		pst.setString(4, props.get("sharp"));
		pst.setString(5, props.get("classification"));
		pst.setString(6, props.get("cloudy"));
		pst.setString(7, props.get("img_big"));
		pst.setString(8, props.get("img_small"));
		pst.setString(9, "");
		pst.setString(10, "");

		BufferedImage image = null;
		
		FileDistributer fd = new FileDistributer(id + "",
				new File("/media/zerr/BA0E0E3E0E0DF3E3/darkskiesimgs/"),21, true);
		
		
		File fimage = fd.extendFile(".jpg");
 		if(!id.startsWith("ISS"))
		{
			int y=0;
			y++;
		}
 		
	
		if (!fimage.exists()) {
		for(int trials=0;trials<3;trials++){
		try {
			
			
				URL url = new URL(task_runinfo__img_small);
				image = ImageIO.read(url);
				break;
		} catch (IOException e) {
			e.printStackTrace();
			Thread.sleep(5000);
			continue;
		}
		
		
		}
		if(image==null){
			fd.removeEmptyFolders();
			continue;}
		ImageIO.write(image, "jpg", fimage);
		}
	
		pst.addBatch();
		

		

	}
	pst.executeBatch();
	
	
	
}catch(Exception e){e.printStackTrace();}finally {
	if(pst!=null)
		try {
  			pst.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}

	}

}
