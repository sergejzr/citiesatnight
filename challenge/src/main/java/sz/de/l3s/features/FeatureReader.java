package sz.de.l3s.features;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class FeatureReader {

	private static Hashtable<String, ftype> featuretypes = new Hashtable<String, ftype>();

	/**
	 * 
	 * @param rs
	 *            should have the fields 'featuretype' and 'text'
	 * @return
	 */
	public Map<String, Map<String, VisualFeature>> readFromDatabaseEntries(ResultSet rs) {
		Map<String, Map<String, VisualFeature>> ret = new Hashtable<String, Map<String, VisualFeature>>();

		try {
			while (rs.next()) {
				String featuretype = rs.getString("featuretype");
				String entry = rs.getString("text");
				Map<String, Map<String, VisualFeature>> fromdb = readFromDatabaseEntry(featuretype, entry);

				combineFeatureSets(ret, featuretype, fromdb);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public void combineFeatureSets(Map<String, Map<String, VisualFeature>> in, String featuretype,
			Map<String, Map<String, VisualFeature>> out) {
		for (String key : out.keySet()) {
			putAll(in, key, out.get(key));
		}
	}

	public void putAll(Map<String, Map<String, VisualFeature>> contiset, String featuretype,
			Map<String, VisualFeature> features) {
		Map<String, VisualFeature> conti = contiset.get(featuretype);
		if (conti == null) {
			contiset.put(featuretype, conti = new HashMap<String, VisualFeature>());
		}
		conti.putAll(features);
	}

	public Map<String, Map<String, VisualFeature>> readFromDatabaseEntry(String featuretype, String databaseentry) {
		Map<String, Map<String, VisualFeature>> ret = new Hashtable<String, Map<String, VisualFeature>>();

		HashMap<String, VisualFeature> conti = new HashMap<String, VisualFeature>();
		for (String featuredesc : databaseentry.split(" ")) {
			String[] pair = featuredesc.split(":");

			String featurename = pair[0];

			if (featurename.trim().length() == 0) {
				continue;
			}

			String strvalue = "1.";

			if (pair.length > 1) {
				strvalue = pair[1];
			} else {
				System.out.println("Wrong entry format");
			}
			Double value = 1.0;
			try {
				value = Double.valueOf(strvalue);

			} catch (Exception e) {
				System.out.println("Wrong number format");
			}

			VisualFeature vf = new VisualFeature(featurename, value);
			conti.put(featurename, vf);
		}
		ret.put(featuretype, conti);
		return ret;
	}

	public Map<String, VisualFeatureGroup> readFeatures(String id, Hashtable<String, String> featuredescriptions) {

		Map<String, VisualFeatureGroup> ret = new HashMap<String, VisualFeatureGroup>();

		for (String featurename : featuredescriptions.keySet()) {
			ftype type = featuretypes.get(featurename);
			String text = featuredescriptions.get(featurename);
			switch (type) {
			case SIFT: {
				ret.put(featurename, getSiftFeature(id, text, featurename));
				break;
			}
			default:
				ret.put(featurename, getFeature(id, text, featurename, type));
			}
		}
		return ret;
	}

	public VisualFeatureGroup readFeatures(String id, String featurename, String featuredescriptiontext) {
		ftype type = featuretypes.get(featurename);
		// HashMap<String, VisualFeatureGroup> curFeatureContainer = new
		// HashMap<String, VisualFeature>();
		String text = featuredescriptiontext;
		switch (type) {
		case SIFT: {
			return (getSiftFeature(id, text, featurename));
		}
		default:
			return (getFeature(id, text, featurename, type));
		}

	}

	enum ftype {
		HISTOGRAMM_TYPE, SINGLE_DOUBLE, SIFT, STRING, HISTOGRAMM_TYPE_DOUBLE
	};

	static {
		featuretypes.put("haarfaces-frontalface_alt-area", ftype.SINGLE_DOUBLE);
		featuretypes.put("edch", ftype.HISTOGRAMM_TYPE);
		featuretypes.put("dog-sift-fkm12k-rnd1M", ftype.SIFT);
		featuretypes.put("haarfaces-profileface-area", ftype.SINGLE_DOUBLE);
		featuretypes.put("sharpness", ftype.SINGLE_DOUBLE);
		featuretypes.put("avg_brightness", ftype.SINGLE_DOUBLE);
		featuretypes.put("hue_stats", ftype.SINGLE_DOUBLE);
		featuretypes.put("naturalness", ftype.SINGLE_DOUBLE);
		featuretypes.put("colorfulness", ftype.SINGLE_DOUBLE);
		featuretypes.put("globalhist_hsv_444", ftype.HISTOGRAMM_TYPE_DOUBLE);
		featuretypes.put("localhist_hsv_44_444", ftype.HISTOGRAMM_TYPE_DOUBLE);
		featuretypes.put("haarfaces-upperbody-count", ftype.SINGLE_DOUBLE);
		featuretypes.put("haarfaces-fullbody-count", ftype.SINGLE_DOUBLE);
		featuretypes.put("haarfaces-lowerbody-count", ftype.SINGLE_DOUBLE);
		featuretypes.put("colorfaces-count", ftype.SINGLE_DOUBLE);
		featuretypes.put("colorfaces-area", ftype.SINGLE_DOUBLE);

	}

	private static VisualFeatureGroup getSiftFeature(String id, String content, String featurename) {

		String[] lines = content.split("\n");
		int startidx = 1;
		HashMap<String, VisualFeature> cntfeatures = new HashMap<String, VisualFeature>();
		if (lines.length > startidx) {
			for (int i = 2; i < lines.length; i++) {

				lines[i] = lines[i].replaceAll("  ", " ").replaceAll("  ", " ");
				String featuresperline[] = lines[i].split(" ");
				if (featuresperline.length < 5) {
					continue;
				}
				String key = featuresperline[4];

				VisualFeature f = cntfeatures.get(featurename + "_" + key);

				if (f == null) {
					cntfeatures.put(featurename + "_" + key, f = new VisualFeature(featurename + "_" + key, 1.));
				}
				f.addFeatureInfo(lines[i]);
				/*
				 * Double cnt=cntfeatures.get(featurename+"_"+key);
				 * if(cnt==null) { cnt=0.; } cnt++;
				 * 
				 * cntfeatures.put(featurename+"_"+key, cnt);
				 */

				//
			}

		}
		for (VisualFeature f : cntfeatures.values()) {
			f.setValue(f.getInfos().size() * 1.);
		}

		VisualFeatureGroup ret = new VisualFeatureGroup(id, featurename,
				lines.length > 1 ? lines[0] + "\n" + lines[0] : lines.length == 1 ? lines[0] : "");
		ret.setFeatures(cntfeatures);
		return ret;
		// return new
		// VisualFeatureGroup(lines.length>1?lines[0]+"\n"+lines[0]:lines.length==1?lines[0]:"",
		// cntfeatures);
	}

	private static VisualFeatureGroup getFeature(String id, String content, String featurename, ftype type) {
		HashMap<String, VisualFeature> cntfeatures = new HashMap<String, VisualFeature>();
		boolean shouldadd = true;
		String[] lines = content.split("\n");
		int startidx = 1;
		boolean featureexists = false;
		if (lines.length > startidx) {
			for (int i = 1; i < lines.length; i++) {

				lines[i] = lines[i].replaceAll("  ", " ").replaceAll("  ", " ");
				String featuresperline[] = lines[i].split(" ");

				int fidx = 0;

				for (String featurevalue : featuresperline) {
					if (featurevalue.trim().length() == 0) {
						continue;
					}
					Double numbertoadd = 0.;
					String curfeaturename = featurename + "_" + i + "_" + fidx;

					switch (type) {
					case SINGLE_DOUBLE: {
						try {
							Double d = Double.parseDouble(featurevalue);
							/*
							 * Double na=d*1000; numbertoadd = na.intValue();
							 */
							numbertoadd = d;
						} catch (NumberFormatException z) {
							System.out.println("not a number");
						}
					}
						break;
					case HISTOGRAMM_TYPE: {
						try {
							Double xval = Double.parseDouble(featurevalue);
							/* Integer intval = xval.intValue(); */
							numbertoadd = xval;

						} catch (NumberFormatException e) {
						}
					}
						break;
					case HISTOGRAMM_TYPE_DOUBLE: {
						try {
							Double d = Double.parseDouble(featurevalue);
							/*
							 * Double na=d*1000;
							 */
							numbertoadd = d;

						} catch (NumberFormatException e) {
						}
					}
						break;
					case STRING: {
						numbertoadd = 1.;
					}
					default: {
						break;
					}
					}
					if (numbertoadd != 0.) {
						featureexists = true;
						shouldadd = false;

						VisualFeature f = cntfeatures.get(curfeaturename);
						if (f == null) {
							cntfeatures.put(curfeaturename, f = new VisualFeature(curfeaturename, numbertoadd));
						}

						// should add here info about the feature. For example
						// its coordinate in the picture
						f.addFeatureInfo("");
						/*
						 * Double cnt=cntfeatures.get(curfeaturename);
						 * if(cnt==null) { cnt=0.; } cnt++;
						 * cntfeatures.put(curfeaturename, numbertoadd);
						 */

					}

					fidx++;

				}
			}
		}
		if (!featureexists) {
			shouldadd = false;

			VisualFeature f = cntfeatures.get(featurename + "_NO");
			if (f == null) {
				cntfeatures.put(featurename + "_NO", f = new VisualFeature(featurename + "_NO", 1.));
			}
			f.addFeatureInfo("");
			

		}
		if (shouldadd) {
			System.out.println("me falta");
		}

		VisualFeatureGroup ret = new VisualFeatureGroup(id, featurename, lines.length > 0 ? lines[0] : "");
		ret.setFeatures(cntfeatures);
		return ret;


	}

	public boolean isValidFeature(String featurename) {
		return featuretypes.containsKey(featurename);
	}

}
