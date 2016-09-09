package uk.soton.iss;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.collections4.bidimap.TreeBidiMap;

import de.l3s.features.Image2Features;
import uk.soton.libsvm.ClassificationResult;
import uk.soton.libsvm.LibSVMArff;

public class ISSImageClassifier {
	public static void main(String[] args) throws IOException {

		// to be called with:
		
		//java  -jar issclassifier.jar  modelpath labelclass imagedirectory
		
		
		ISSImageClassifier ic = new ISSImageClassifier();
		ic.classifyMulti(args);
	}

	private void classifyMulti(String[] args) throws IOException {

		String classifiernames[] = args[0].split(",");
		// String arffname=args[1];
		String classattribute = args[1];
		String images[] = args[2].split(",");

		String classattributes[] = classattribute.split(",");

		Hashtable<String, Hashtable<String, Hashtable<String, Double>>> csv = new Hashtable<>();

		Hashtable<String, LibSVMArff> classifiers = new Hashtable<>();
		int cidx = 0;

		Image2Features ife = new Image2Features();

		ArrayList<String> toclassify = new ArrayList<>();

		for (String image : images) {
			if (!image.startsWith("http://")) {
				File imagedirfile = new File(image);
				if (imagedirfile.isDirectory()) {
					for (File f : imagedirfile.listFiles()) {
						toclassify.add(f.toString());
					}

				} else {
					toclassify.add(imagedirfile.toString());
				}
			} else {
				toclassify.add(image);
			}
		}

		String photos[] = new String[toclassify.size()];
		toclassify.toArray(photos);

		
		File arfffile=new File(classifiernames[0] + ".arff");
		
		InputStream arffstream = null;
		
		
		if(!arfffile.exists()){
			//arffstream=ISSImageClassifier.class.getResourceAsStream(classifiernames[0] + ".arff");
		}else
		{
			 arffstream = new FileInputStream(arfffile);
		}
		
	//	InputStream arffstream = ISSImageClassifier.class.getResourceAsStream(classifiernames[0] + ".arff");

		String sb = ife.toArff(arffstream, photos, null);
		System.out.println("challenge\n"+sb);
		
		
		
		for (String classifiername : classifiernames) {

			File modelfile=new File(classifiername + ".svmlighmodel");
			 arfffile=new File(classifiername + ".arff");
			
			 arffstream = null;
			InputStream modelstream=null;
			
			if(!arfffile.exists()){
			//	arffstream=ISSImageClassifier.class.getResourceAsStream(classifiername + ".arff");
		//	modelstream = ISSImageClassifier.class.getResourceAsStream(classifiername + ".svmlighmodel");
			}else
			{
				 arffstream = new FileInputStream(arfffile);
				 modelstream=new FileInputStream(modelfile);
			}
			
			
			try {
				LibSVMArff libsvm = new LibSVMArff(arffstream, modelstream, classattributes[cidx]);
				cidx++;
				classifiers.put(classifiername, libsvm);
				arffstream.close();
				modelstream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		for (String curclassifiername : classifiers.keySet()) {
			LibSVMArff libsvm = classifiers.get(curclassifiername);
			List<ClassificationResult> results = libsvm.classifyInstance(sb);
			int idx = 0;
			List<String> classlablesstr = null;
			for (ClassificationResult res : results) {
				TreeBidiMap<String, Double> classlables = res.getClasslables();

				Hashtable<String, Double> scores = new Hashtable<>();

				if (classlablesstr == null) {
					classlablesstr = new ArrayList<>();
					classlablesstr.addAll(classlables.keySet());
					System.out.print("imagepath\tpredictedclass");

					for (String s : classlablesstr) {
						System.out.print("\t" + s);
					}
					System.out.println();
				}
				for (String lab : classlables.keySet()) {
					try {
						scores.put(lab, res.getProbabilityForClass(classlables.get(lab)));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				List<String> sorted = new ArrayList<>();
				sorted.addAll(scores.keySet());

				Collections.sort(sorted, new de.l3s.util.AssociativeComparator(scores));
				Collections.reverse(sorted);

				System.out.print("'" + toclassify.get(idx).toString() + "'\t" + "'" + sorted.get(0) + "'");

				for (String s : classlablesstr) {
					System.out.print("\t" + scores.get(s));
				}
				System.out.println();

				idx++;
			}
		}

	}



}
