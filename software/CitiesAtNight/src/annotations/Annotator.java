package annotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import db.DB;

public class Annotator {
	
	public static void main(String[] args) {
		Annotator an=new Annotator();
		an.annotate(5,80);
	}

	private void annotate(int users, int confidence) {
		try {
			Connection con = DB.getLocalConnection();

			
			Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> records=new Hashtable<>();
			Hashtable<String, Vector<String>> userannotations=new Hashtable<>();
			
		Statement st = con.createStatement();
		
		ResultSet rs = st.executeQuery("SELECT * FROM citiesatnight.darkskies");
		
		while(rs.next())
		{
		String img_id=rs.getString("img_id");
			Vector<String> conti = userannotations.get(img_id);
			if(conti==null)
			{
				userannotations.put(img_id, conti=new Vector<>());
				
			}
			conti.add(rs.getString("task_run__user_id"));
			
		addlab("sharp",rs,records);
		addlab("classification",rs,records);
		addlab("cloudy",rs,records);
		}
		rs.close();
		PreparedStatement pst = con.prepareStatement("INSERT INTO "
				+ "citiesatnight.groundtruth_"+users+"_"+confidence+"_classification "
				+ "(id,classification,labelclass, labelsharp, labelcloudy) "
				+ "VALUES (?,?,?,?,?)");
		Hashtable<String, Integer> countclasses=new Hashtable<>();
	HashSet<String> sharp=new HashSet<>(Arrays.asList("blurry,sharp".split(",")));
	HashSet<String> cloudy=new HashSet<>(Arrays.asList("someclouds,cloudy,clear".split(",")));
	HashSet<String> classifications=new HashSet<>(Arrays.asList("aurora,stars,astronaut,black,city,none".split(",")));
	
	
		int cntbatch=1;		
		for(String img_id:records.keySet())
		{

			Hashtable<String, Hashtable<String, Integer>> record = records.get(img_id);
			Hashtable<String, Double> scoresummary=new Hashtable<>();
			for(String k:new String[]{"classification","sharp","cloudy"}){
			Hashtable<String, Integer> classification = record.get(k);
			if(classification==null) continue;
			Score classscore=decideLable(classification,userannotations.get(img_id));
			String lab = classscore.getValidScore(users,confidence/100.);
			if(lab!=null)
			scoresummary.put(lab, classscore.getScore(lab));
			}
			pst.setString(3,null);
			pst.setString(4,null);
			pst.setString(5,null);
			if(scoresummary.size()>0)
			{
				for(String lab:scoresummary.keySet())
				{
					Integer cnt = countclasses.get(lab);
					if(cnt==null)
					{
						cnt=0;
					}
					
					countclasses.put(lab, cnt+1);
					
					if(classifications.contains(lab))
					{
						pst.setString(3, lab);
					}else if (sharp.contains(lab))
					{
						pst.setString(4, lab);
					}else if (cloudy.contains(lab))
					{
						pst.setString(5, lab);
					}
				}
				
				pst.setString(1, img_id);
				pst.setString(2, records.get(img_id).toString());
				pst.addBatch();
				
				if(cntbatch++>1000)
				{
					pst.executeBatch();
				}
				
			}
			/*
			Double classification = scoresummary.get("city");
			if(scoresummary.size()>0 &&classification!=null)
			System.out.println(img_id+" "+records.get(img_id)+" "+scoresummary);
			*/
			
			
		}
		pst.executeBatch();
		pst.close();
		System.out.println(countclasses);	
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private Score decideLable(Hashtable<String, Integer> classification, Vector<String> vector) {
	
		Score ret=new Score(classification,vector);
		return ret;
	}

	private void addlab(String art, ResultSet rs,
			Hashtable<String, Hashtable<String, Hashtable<String, Integer>>> records) throws SQLException {
		String img_id = rs.getString("img_id");
		String val = rs.getString(art);
		if(val==null||val.length()==0) return;
		Hashtable<String, Hashtable<String, Integer>> record = records.get(img_id);
		
		if(record==null)
		{
			records.put(img_id, record=new Hashtable<>());
		}
		
		Hashtable<String, Integer> prop = record.get(art);
		
		if(prop==null)
		{
			record.put(art, prop=new Hashtable<>());
		}
		
		Integer cnt = prop.get(val);
		if(cnt==null){cnt=0;}
		
		prop.put(val, cnt+1);
		
	}

}
