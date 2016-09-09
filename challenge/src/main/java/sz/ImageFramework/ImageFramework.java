package sz.ImageFramework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.collections4.bidimap.TreeBidiMap;

import features.Image2Features;
import sz.libsvm.ClassificationResult;
import sz.libsvm.LibSVMArff;

public class ImageFramework {
public static void main(String[] args) {
	String command=args[0];
	
	
	if(command.equals("extract"))
	{//extract /media/zerr/BA0E0E3E0E0DF3E3/darkskies/test.arff /media/zerr/BA0E0E3E0E0DF3E3/darkskiesimgs/000/000/00I/SS0/11-/E-9/175/00000000ISS011-E-9175.jpg
		
	
		
		
		try {
			extract(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}
	
	if(command.equals("train"))
	{
		//train /media/zerr/BA0E0E3E0E0DF3E3/darkskies/labelclass_5_08_500.arff /media/zerr/BA0E0E3E0E0DF3E3/darkskies/threemodeldir/newmodeltodelete/ labelclass labelclass
		
		train(args);
		return;
	}
	
	if(command.equals("classify"))
	{//classify /media/zerr/BA0E0E3E0E0DF3E3/darkskies/5_1_short_arff.arff /media/zerr/BA0E0E3E0E0DF3E3/darkskies/testmodeldir testmodel labelclass /media/zerr/BA0E0E3E0E0DF3E3/darkskiesimgs/000/000/00I/SS0/11-/E-9/175/00000000ISS011-E-9175.jpg
		
		
		try {
			classify(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}
	
	
}



private static void train(String[] args) {
	
	File arffile = new File(args[1]);
	File modeloutputdir=new File(args[2]);
	String modelname=args[3];
	String classattribute=args[4];
	
	try {
		LibSVMArff libsvm=new LibSVMArff( arffile, null,  modeloutputdir,  modelname, classattribute);
		libsvm.trainFromArff();
		libsvm.store();	
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	
	
}

private static void extract(String[] args) throws IOException {
	Image2Features ife=new Image2Features();
	File arffile = new File(args[1]);
	File imag = new File(
			args[2]);
	
	File arffout=null;
	if(args.length>3){arffout=new File(args[3]);}
	String sb = ife.toArff(arffile,imag, arffout);
	//System.out.println(sb);
}



private static void classify(String[] args) throws IOException {
	
	File arffile = new File(args[1]);
	File modeloutputdir=new File(args[2]);
	String modelname=args[3];
	String classattribute=args[4];
	File imag = new File(
			args[5]);
	
	ArrayList<File> filestoclassify=new ArrayList<File>();
	
	if(imag.isDirectory())
	{
		filestoclassify.addAll(Arrays.asList(imag.listFiles()));
	}else
	{
		filestoclassify.add(imag);
	}
	Image2Features ife=new Image2Features();
	String sb = ife.toArff(arffile,imag, null);
	
	
	try {
		LibSVMArff libsvm=new LibSVMArff(arffile, null,  modeloutputdir,  modelname, classattribute);
		
		 List<ClassificationResult> results = libsvm.classifyInstance(sb);
		int idx=0;
		 for(ClassificationResult res:results)
		 {
			 TreeBidiMap<String, Double> classlables = res.getClasslables();
				
				Hashtable<String, Double> scores=new Hashtable<String, Double>();
				for(String lab:classlables.keySet())
				{
					try {
						scores.put(lab, res.getProbabilityForClass(classlables.get(lab)));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				List<String> sorted=new ArrayList<String>();
				sorted.addAll(scores.keySet());
				
				Collections.sort(sorted, new de.l3s.util.datatypes.comparators.AssociativeComparator(scores));
				Collections.reverse(sorted);
				System.out.print("'"+filestoclassify.get(idx).toString()+"'\t");
				for(String s:sorted)
				{
					System.out.print(s+":"+scores.get(s)+" ");
				}
				System.out.println();
				idx++;
		 }
		
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	
	
}

}
