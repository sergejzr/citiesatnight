package features;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import database.MegaInserter;
import db.DB;
import de.l3s.features.FeatureReader;
import de.l3s.features.VisualFeature;
import de.l3s.features.VisualFeatureGroup;
import de.l3s.image.tools.FeatureCreator;
import de.l3s.image.tools.FeatureExtractionWorker;
import de.l3s.image.tools.FeatureListener;
import de.l3s.image.tools.TTask;
import de.l3s.image.tools.TWorker;
import de.l3s.image.tools.ThreadedWorker;
import de.l3s.util.image.FileDistributer;

public class FeatureExtractor implements FeatureListener {
	FeatureReader fr = new FeatureReader();
	FileWriter fw;
	private File imageInputDirectory;
	private File featureFile;
	private String tablename;
	private HashSet<String> availableIds;

	private String sql;

	public FeatureExtractor(File imageInputDirectory, File featureFile, String tablename, String sql)
			throws IOException {
		this.imageInputDirectory = imageInputDirectory;
		this.featureFile = featureFile;
		this.tablename = tablename;
	

		this.sql = sql;
		System.out.println("Get images from " + imageInputDirectory + " described in " + tablename + " for the sql: "
				+ sql + " write the features into the file: " + featureFile);
	}

	public HashSet<String> getAvailableIds() {
		HashSet<String> ret = new HashSet<String>();
		try {

			Connection con=null;
			try {
				con = DB.getLocalConnection();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			PreparedStatement pst = con.prepareStatement("SELECT DISTINCT photoid from " + tablename + " ");
			ResultSet rs = pst.executeQuery();
			/*
			 * if(!rs.next()){ return ret;}
			 */
			while (rs.next()) {
				ret.add(rs.getString("photoid"));
			}
			rs.close();
			pst.close();
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	public void run() throws IOException {

		if (!featureFile.exists() || featureFile.length() < 2000) {

			fw = new FileWriter(featureFile);

			FeatureCreator fc = new FeatureCreator(8);
			availableIds = getAvailableIds();
			gothrough(sql);
			// fc.gothrough(imageInputDirectory, this);

			fw.flush();
			fw.close();

		} else {
			log().info("SQL file already exists, execute.");
		}
		if (tablename != null) {
			log().info("will now insert the data from " + featureFile.getAbsolutePath() + " to " + tablename);
			try {

				DB.getLocalConnection().createStatement()
						.execute("LOAD DATA LOCAL INFILE '" + featureFile.getAbsolutePath() + "' IGNORE  INTO TABLE "
								+ tablename + " FIELDS ENCLOSED BY '\"'");
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		log().info("ready");

		System.out.println("Optionaly use following statement to insert the data into DB:\n"
				+ "mysql -h oracle.l3s.uni-hannover.de -u zerr -p\n" + "LOAD DATA LOCAL INFILE '"
				+ featureFile.getAbsolutePath() + "' IGNORE  INTO TABLE tbl_name "

		);
	}

	private void gothrough(String sql) {
		try {
			ThreadedWorker t = new ThreadedWorker(10);
			Connection con=null;
			try {
				con = DB.getLocalConnection();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Statement st = con.createStatement();

			ResultSet rs = st.executeQuery("SELECT COUNT(*) as cnt FROM (" + sql + ") k");
			rs.next();
			int cnt = rs.getInt("cnt");
			rs.close();
			st.close();

			int backet = 50000;
			for (int i = 0; i < cnt + 100; i += backet) {
				System.out.println("Overall there are " + cnt + " photos to process");
				st = con.createStatement();
				rs = st.executeQuery("SELECT * FROM (" + sql + ") j LIMIT " + i + ", " + backet);

				while (rs.next()) {
					String photoid = rs.getString("photoid");
					if (availableIds.contains(photoid)) {
						continue;
					}
					TWorker worker = t.getWorker();

					FileDistributer fd = new FileDistributer(photoid, (imageInputDirectory), false);

					worker.executeTask(getTask(fd.extendFile(".jpg")));

				}
				rs.close();
				st.close();
			}
			t.finish();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		try {
			String tag = "boat";
			String table = "citiesatnight.photovisualfeatures";
			FeatureExtractor if2 = new FeatureExtractor(
					new File("/media/zerr/BA0E0E3E0E0DF3E3/darkskiesimgs/"),
					new File("/media/zerr/BA0E0E3E0E0DF3E3/darkskies/" + 23532 + "_clean.fdata"), table,

			"SELECT p.img_id as photoid FROM citiesatnight.darkskies p LEFT JOIN " + table
					+ " f ON ( f.photoid = p.img_id ) WHERE f.photoid IS NULL"

			);
			if2.run();
			/*
			 * if2= new Table2Features2SQLFile( new
			 * File("/data2/zerr/recentpublicimages_test_clean"), new
			 * File("/data2/zerr/recentpublicimages_test_clean.fdata"
			 * ),"flickrcrawl.photovisualfeatures_tagged" ); if2.run();
			 */

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	int cntphotois = 0;
	MegaInserter mi;

	static int cntdone = 0;
	Hashtable<String, Hashtable<String, String>> featurestowrite = new Hashtable<String, Hashtable<String, String>>();
	int statementlength = 0;

	@Override
	public void featuresRead(String id, Hashtable<String, String> featuredescriptions) {

		// if(availableIds.contains(id)){return;}

		Map<String, VisualFeatureGroup> vfeatures = fr.readFeatures(id, featuredescriptions);
		try {

			writeout(id, vfeatures);
			// availableIds.add(id);
		}

		catch (IOException e) {
			e.printStackTrace();
			log().error(id + "caused an IO error ");
		}

		cntphotois++;
		if (cntphotois % 100 == 0) {
			log().info("Got " + cntphotois + " ids: " + (new Date().toString()));
		}

	}

	private synchronized void writeout(String id, Map<String, VisualFeatureGroup> vfeatures) throws IOException {
		for (String fname : vfeatures.keySet()) {

			StringBuilder sb = new StringBuilder();
			Map<? extends String, ? extends VisualFeature> fconti = vfeatures.get(fname).getFeatures();

			fw.write('"');
			fw.write(id);
			fw.write('"');
			fw.write("\t");
			fw.write('"');
			fw.write(fname);
			fw.write('"');
			fw.write("\t");
			fw.write('"');
			boolean first = true;
			for (VisualFeature f : fconti.values()) {

				if (!first) {
					fw.write(" ");
				}
				first = false;

				fw.write(f.getFeaturename());
				fw.write(":");
				fw.write(f.getValue() + "");
			}
			fw.write('"');
			fw.write("\n");
		}
	}

	private Logger log() {
		return LogManager.getLogger(FeatureExtractor.class);

	}

	@Override
	public void ready() {

	}

	@Override
	public boolean accept(File cur) {
		return !cur.getAbsoluteFile().toString().endsWith(".jpg");
	}

	@Override
	public TTask getTask(File cur) {

		return new FeatureExtractionWorker(cur, null, this);
	}

}
