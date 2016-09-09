package uk.soton.iss;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.collections4.bidimap.TreeBidiMap;

import features.Image2Features;
import sz.libsvm.ClassificationResult;
import sz.libsvm.LibSVMArff;

public class ISSImageClassifier {
public static void main(String[] args) throws IOException {
	
	//labelclass labelclass http://www.esa.int/var/esa/storage/images/esa_multimedia/images/2015/11/the_last_waltz/15699245-1-eng-GB/The_last_waltz_node_full_image_2.jpg,http://www.esa.int/var/esa/storage/images/esa_multimedia/images/2016/01/magical_aurora/15774108-1-eng-GB/Magical_aurora_node_full_image_2.jpg,http://www.esa.int/var/esa/storage/images/esa_multimedia/images/2012/04/paris_by_night/9912230-2-eng-GB/Paris_by_night_node_full_image_2.jpg,http://www.esa.int/var/esa/storage/images/esa_multimedia/images/2012/04/the_uk_and_ireland_at_night/10275532-2-eng-GB/The_UK_and_Ireland_at_night_node_full_image_2.jpg,http://www.esa.int/var/esa/storage/images/esa_multimedia/images/2015/04/make_a_wish/15350827-1-eng-GB/Make_a_wish_node_full_image_2.jpg,http://www.esa.int/var/esa/storage/images/esa_multimedia/images/2012/04/monument_valley_las_vegas_and_the_colorado_river_seen_from_the_iss/9668543-3-eng-GB/Monument_Valley_Las_Vegas_and_the_Colorado_river_seen_from_the_ISS_node_full_image_2.jpg
	ISSImageClassifier ic=new ISSImageClassifier();
	ic.classifyMulti(args);
}


private  void classifyMulti(String[] args) throws IOException {
	
	String classifiernames[]=args[0].split(",");
	//String arffname=args[1];
	String classattribute=args[1];
	String images[] = 
			args[2].split(",");
	
	
	String classattributes[]=classattribute.split(",");
	
	
	
Hashtable<String, Hashtable<String, Hashtable<String, Double>>> csv=new Hashtable<>();
	
	
	
	

	Hashtable<String,LibSVMArff> classifiers=new Hashtable<>();
int cidx=0;
	for(String classifiername: classifiernames)
	{
		
		InputStream arffstream = ISSImageClassifier.class
				.getResourceAsStream(classifiername+".arff");
	InputStream modelstream = ISSImageClassifier.class
			.getResourceAsStream(classifiername+".svmlighmodel");
	try {
		LibSVMArff libsvm=new LibSVMArff(arffstream, modelstream, classattributes[cidx]);
		cidx++;
		classifiers.put(classifiername, libsvm);
		arffstream.close();
		modelstream.close();
	}catch(IOException e)
	{
		e.printStackTrace();
	}
	}
	Image2Features ife=new Image2Features();
	
	
	
	
	ArrayList<String> toclassify=new ArrayList<>();
		
		for(String image:images){
		if(!image.startsWith("http://")){
		File imagedirfile=new File(image);
		if(imagedirfile.isDirectory())
		{
			for(File f:imagedirfile.listFiles())
			{
				toclassify.add(f.toString());
			}
			
		}else
		{
			toclassify.add(imagedirfile.toString());
		}
		}else
		{
			toclassify.add(image);
		}
		}
		
		String photos[]=new String[toclassify.size()];
		toclassify.toArray(photos);
		
		InputStream arffstream = ISSImageClassifier.class
				.getResourceAsStream(classifiernames[0]+".arff");
		 
		String sb = ife.toArff(arffstream,photos, null);
		
		for(String curclassifiername:classifiers.keySet()){
			LibSVMArff libsvm = classifiers.get(curclassifiername);
		 List<ClassificationResult> results = libsvm.classifyInstance(sb);
		int idx=0;
	List<String> classlablesstr=null;
		 for(ClassificationResult res:results)
		 {
			 TreeBidiMap<String, Double> classlables = res.getClasslables();
				
				Hashtable<String, Double> scores=new Hashtable<>();
				
				if(classlablesstr==null){
					classlablesstr=new ArrayList<>(); classlablesstr.addAll(classlables.keySet());
				System.out.print("imagepath\tpredictedclass");
				
				for(String s:classlablesstr)
				{
					System.out.print("\t"+s);
				}
				System.out.println();
				}
				for(String lab:classlables.keySet())
				{
					try {
						scores.put(lab, res.getProbabilityForClass(classlables.get(lab)));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				List<String> sorted=new ArrayList<>();
				sorted.addAll(scores.keySet());
				
				Collections.sort(sorted, new de.l3s.util.datatypes.comparators.AssociativeComparator(scores));
				Collections.reverse(sorted);
				
				System.out.print("'"+toclassify.get(idx).toString()+"'\t"+"'"+sorted.get(0)+"'");
				
				for(String s:classlablesstr)
				{
					System.out.print("\t"+scores.get(s));
				}
				System.out.println();
				

				idx++;
		 }
		}
		
	
	
	
}


private  void classifyMulti_old(String[] args) throws IOException {
	
	String classifiernames[]=args[0].split(",");
	//String arffname=args[1];
	String classattribute=args[1];
	String images[] = 
			args[2].split(",");
	
	
	String classattributes[]=classattribute.split(",");
	
	
	
Hashtable<String, Hashtable<String, Hashtable<String, Double>>> csv=new Hashtable<>();
	
	
	
	

	Hashtable<String,LibSVMArff> classifiers=new Hashtable<>();
int cidx=0;
	for(String classifiername: classifiernames)
	{
		
		InputStream arffstream = ISSImageClassifier.class
				.getResourceAsStream(classifiername+".arff");
	InputStream modelstream = ISSImageClassifier.class
			.getResourceAsStream(classifiername+".svmlighmodel");
	try {
		LibSVMArff libsvm=new LibSVMArff(arffstream, modelstream, classattributes[cidx]);
		cidx++;
		classifiers.put(classifiername, libsvm);
		arffstream.close();
		modelstream.close();
	}catch(IOException e)
	{
		e.printStackTrace();
	}
	}
	Image2Features ife=new Image2Features();
	
	
	
	
	ArrayList<String> toclassify=new ArrayList<>();
		
		for(String image:images){
		if(!image.startsWith("http://")){
		File imagedirfile=new File(image);
		if(imagedirfile.isDirectory())
		{
			for(File f:imagedirfile.listFiles())
			{
				toclassify.add(f.toString());
			}
			
		}else
		{
			toclassify.add(imagedirfile.toString());
		}
		}else
		{
			toclassify.add(image);
		}
		}
		
		String photos[]=new String[toclassify.size()];
		toclassify.toArray(photos);
		
		InputStream arffstream = ISSImageClassifier.class
				.getResourceAsStream(classifiernames[0]+".arff");
		 
		String sb = ife.toArff(arffstream,photos, null);
		
		for(String curclassifiername:classifiers.keySet()){
			LibSVMArff libsvm = classifiers.get(curclassifiername);
		 List<ClassificationResult> results = libsvm.classifyInstance(sb);
		int idx=0;
		
		 for(ClassificationResult res:results)
		 {
			 TreeBidiMap<String, Double> classlables = res.getClasslables();
				
				Hashtable<String, Double> scores=new Hashtable<>();
				for(String lab:classlables.keySet())
				{
					try {
						scores.put(lab, res.getProbabilityForClass(classlables.get(lab)));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				List<String> sorted=new ArrayList<>();
				sorted.addAll(scores.keySet());
				
				Collections.sort(sorted, new de.l3s.util.datatypes.comparators.AssociativeComparator(scores));
				Collections.reverse(sorted);

				 Hashtable<String, Hashtable<String, Double>> imgconti = csv.get(toclassify.get(idx).toString());
				if(imgconti==null)
				{
					csv.put(toclassify.get(idx).toString(), imgconti=new Hashtable<>());
				}
				Hashtable<String, Double> scoreconti;
				imgconti.put(curclassifiername, scoreconti=new Hashtable<>());
				scoreconti.put(sorted.get(0),scores.get(sorted.get(0)));

				idx++;
		 }
		}
		
	
	for(String imagekey:csv.keySet())
	{
		Hashtable<String, Hashtable<String, Double>> conti = csv.get(imagekey);
		System.out.print("'"+imagekey+"'");
		
		for(String classifiername:conti.keySet())
		{
			Hashtable<String, Double> score = conti.get(classifiername);
			
			System.out.print("\t"+score.keys().nextElement()+" ");
		}
		System.out.println();
	}
	
}

}
