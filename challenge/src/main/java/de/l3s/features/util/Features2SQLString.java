package de.l3s.features.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import de.l3s.features.VisualFeature;
import de.l3s.features.VisualFeatureGroup;

public class Features2SQLString {

	public static Hashtable<String, String>  toSQLString(String id,
			Map<String, VisualFeatureGroup> vfeatures) throws IOException {
		
		Hashtable<String, String> sqlFiles=new Hashtable<String, String>();
		for (String fname : vfeatures.keySet()) {

			StringBuilder sb = new StringBuilder();
			Map<? extends String, ? extends VisualFeature> fconti = vfeatures
					.get(fname).getFeatures();

			sb.append('"');
			sb.append(id);
			sb.append('"');
			sb.append("\t");
			sb.append('"');
			sb.append(fname);
			sb.append('"');
			sb.append("\t");
			sb.append('"');
			boolean first = true;
			for (VisualFeature f : fconti.values()) {

				if (!first) {
					sb.append(" ");
				}
				first = false;

				sb.append(f.getFeaturename());
				sb.append(":");
				sb.append(f.getValue() + "");
			}
			sb.append('"');
			sb.append("\n");
			sqlFiles.put(fname, sb.toString());
		}
		return sqlFiles;
	}

	public static Map<String, VisualFeatureGroup> fromSQLFile(String filestr) {
		Map<String, VisualFeatureGroup> ret=new HashMap<String, VisualFeatureGroup>();
		String[] lines=filestr.split("\n");
		for(String line:lines)
		{
			String[] columns = line.split("\t");
			
			String fname=strip(columns[1]);
			VisualFeatureGroup gr = ret.get(fname);
			
			if(gr==null)
			{
				String metadata="";
				ret.put(fname, gr=new VisualFeatureGroup(columns[0], fname, metadata));
				
			}
			
			String featuresstr=strip(columns[2]);
			String[] features = featuresstr.split(" ");
			
			for(String feature:features)
			{
				try{
				String[] keyval = feature.split(":");
				gr.addFeature(keyval[0], keyval[1]);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return ret;
	}

	private static String strip(String string) {
		
		return string.substring(1,string.length()-1);
	}
}
