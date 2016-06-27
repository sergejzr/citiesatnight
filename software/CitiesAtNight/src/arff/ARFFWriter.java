package arff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.apache.batik.dom.util.HashTable;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import db.DB;

public class ARFFWriter {

	public static void main(String[] args) {
		
		ARFFWriter arrw = new ARFFWriter();
		String score="5_70";
		String groundtruthtable="citiesatnight.groundtruth_"+score+"_classification";
		String arffname=score+"_arff";
		
		arrw.create(groundtruthtable,arffname,1000,"/media/zerr/BA0E0E3E0E0DF3E3/darkskies/");
	}

	private void create(String groundtruthtable, String arffname, Integer limit, String outputdir) {
		try {
			Connection con = DB.getLocalConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT id, labelclass, labelsharp, labelcloudy FROM "+groundtruthtable);
			Hashtable<String, Hashtable<String, String>> rows = new Hashtable<>();
			List<List<String>> idbatches = new ArrayList<>();
			List<String> curbatch = new ArrayList<>();
			BidiMap<String, Integer> idx= new DualHashBidiMap<>();
			Hashtable<String,HashSet<String>> balancer=new Hashtable<>();
			int cntg=0;
			
			while (rs.next()) {
				String id = rs.getString("id");
				String labelclass = rs.getString("labelclass");
				String labelsharp = rs.getString("labelsharp");
				String labelcloudy = rs.getString("labelcloudy");

				Hashtable<String, String> vals = new Hashtable<>();
				
				if(labelclass!=null){
				vals.put("labelclass", labelclass);
				}
				else
				{
					continue;
				}
				rows.put(id, vals);
				
				
				
				if(labelclass==null) continue;
				HashSet<String> balancerconti = balancer.get(labelclass);
				
				if(balancerconti==null){
				balancer.put(labelclass, balancerconti=new HashSet<>());
				}
				
				if(limit!=null&&balancerconti.size()>=limit){
				continue;
				}
				balancerconti.add(id);
				/*
				if(labelsharp!=null)
				vals.put("labelsharp", labelsharp);
				if(labelcloudy!=null)
				vals.put("labelcloudy", labelcloudy);
				*/
				/*
				for(String f:vals.keySet())
				{
					Integer intidx = idx.get(f);
					if(intidx==null) intidx=idx.size()+1;
					idx.put(f,intidx);
				}
*/
				if (curbatch.size() > 100) {

					idbatches.add(curbatch);
					curbatch = new ArrayList<>();

				}
				curbatch.add(id);
if(cntg++%100==0)
{
	System.out.println(cntg+" gt images done");
	}
			}
			idbatches.add(curbatch);
			rs.close();
			st.close();
			con.close();
		
			System.out.println("There are: "+idbatches.size()+"batches");	
		Hashtable<String, Hashtable<String, String>> featuredimg=new Hashtable<>();
		
		
		
		int cntb=0;
			for (List<String> batch : idbatches) {
				if(cntb++%10==0)
				{
					System.out.println(cntb+" feature batches read (corresponds to"+(cntb*batch.size())+" photos)");
					}
				
				 con = DB.getLocalConnection();
				st = con.createStatement();
				
String sql = "SELECT * FROM citiesatnight.photovisualfeatures WHERE photoid in ("
		+ tostringlist(batch) + ") ";

//System.out.println(sql);
				rs = st.executeQuery(sql);
			//	idx.put("id", 1);
				while (rs.next()) {
					try{
					String photoid = rs.getString("photoid");
					String features = rs.getString("features");
					if(features.length()==0)
					{
						System.out.println("no "+rs.getString("fname")+" features for "+photoid);
						continue;
					}

					
					System.out.println(features.length());
					String[] fpairs = features.split("\\s+");
					Hashtable<String, String> conti = featuredimg.get(photoid);
					if(conti==null) {featuredimg.put(photoid, conti=new Hashtable<>());}
					
					for(String fpairstr:fpairs)
					{
						String[] featurepairarr = fpairstr.split(":");
						String fname = featurepairarr[0].replace("\"", "");
						conti.put(fname, featurepairarr[1]);
						
						Integer intidx = idx.get(fname);
						if(intidx==null) intidx=idx.size();
						idx.put(fname,intidx);
					}
					}catch(Exception e)
					{
						e.printStackTrace();
					}

				}
				rs.close();
				st.close();
con.close();
			}
			System.out.println("Start writing arff");
			try {
				FileWriter fw=new FileWriter(new File(new File(outputdir),arffname+".arff"));
				fw.write("@RELATION ISSIMages\n\n");
		
				fw.write("@ATTRIBUTE id string\n");
				fw.write("@ATTRIBUTE labelclass {city,astronaut,aurora,black,none,stars}\n");
				fw.write("@ATTRIBUTE labelsharp {sharp,blurry}\n");
				fw.write("@ATTRIBUTE labelcloudy {cloudy,clear,someclouds}\n");
				System.out.println("Start writing attributes");
				for(int i=0;i<idx.size();i++)
				{
					 String dim = idx.getKey(i);
					
					fw.write("@ATTRIBUTE "+dim+" NUMERIC\n");
				}
				
				fw.write("@DATA\n\n");
				System.out.println("Start writing data");
				int countd=0;
				for(String img_id: rows.keySet())
				{
					if(countd++%100==0)
					{
						System.out.println(countd+" photos written into arff");
						}
					
					Hashtable<String, String> features = featuredimg.get(img_id);
					if(features==null) continue;
					Hashtable<String, String> row = rows.get(img_id);
					
					String lab = row.get("labelclass");
					
					
					
					
					
					
					
					
					fw.write("{0 '"+img_id+"',");
				
					if(row.get("labelclass")!=null){
						
					fw.write("1 ");
				
					fw.write(lab==null?"notdefined":lab);
					}else
					{
						continue;
					}

			
					if(false&&row.get("labelsharp")!=null){
						fw.write(",");
					fw.write("2 ");
					fw.write(row.get("labelsharp")==null?"notdefined":row.get("labelsharp"));
				
					}
				
					
					if(false&&row.get("labelcloudy")!=null){
						fw.write(",");
					String last = row.get("labelcloudy")==null?"notdefined":row.get("labelcloudy");
					fw.write("3 "+last);
					
					}
					 
					
					if(features!=null){
						for(int i=0;i<idx.size();i++)
						{
							String key=idx.getKey(i);
							if(features.containsKey(key))
							{
								int shiftidx = i+4;
								fw.write(","+shiftidx+" "+features.get(key));
							}
						}

					}
					fw.write("}\n");
					
					
				}
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String tostringlist(List<String> batch) {
		StringBuilder sb = new StringBuilder();

		for (String s : batch) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append("'");
			sb.append(s);
			sb.append("'");
		}
		// TODO Auto-generated method stub
		return sb.toString();
	}
}
