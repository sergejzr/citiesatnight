package uk.soton.iss;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.collections4.bidimap.TreeBidiMap;

import de.l3s.features.Image2Features;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import uk.soton.libsvm.ClassificationResult;
import uk.soton.libsvm.LibSVMArff;

public class ISSImageClassifier {
	
	
	
	public static void main(String[] args) throws IOException {

		// to be called with:

		// java -jar issclassifier.jar modelpath labelclass imagedirectory

		ISSImageClassifier ic = new ISSImageClassifier();
		Options opt = ic.getopt(args);
		if(!opt.isValid())
		{
			System.out.println("Invalid usage: ");
			return;
		}
		ic.classifyMulti(opt);
	}

	private void classifyMulti(Options opt) throws IOException {

		String classifiernames[] = opt.getModeldir().split(",");
		// String arffname=args[1];
		String classattribute =opt.getClassattribute();
		String images[] = opt.getImagedir().split(",");

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

		File arfffile = new File(classifiernames[0] + ".arff");

		InputStream arffstream = null;

		if (!arfffile.exists()) {
			arffstream = ISSImageClassifier.class.getResourceAsStream(classifiernames[0] + ".arff");
		} else {
			arffstream = new FileInputStream(arfffile);
		}

		// InputStream arffstream =
		// ISSImageClassifier.class.getResourceAsStream(classifiernames[0] +
		// ".arff");

		String sb = ife.toArff(arffstream, photos, null);
		// System.out.println("challenge\n"+sb);

		for (String classifiername : classifiernames) {

			File modelfile = new File(classifiername + ".svmlighmodel");
			arfffile = new File(classifiername + ".arff");

			arffstream = null;
			InputStream modelstream = null;

			if (!arfffile.exists()) {
				arffstream = ISSImageClassifier.class.getResourceAsStream(classifiername + ".arff");
				modelstream = ISSImageClassifier.class.getResourceAsStream(classifiername + ".svmlighmodel");
			} else {
				arffstream = new FileInputStream(arfffile);
				modelstream = new FileInputStream(modelfile);
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

		StringBuilder result = new StringBuilder();
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
					result.append("imagepath\tpredictedclass");

					for (String s : classlablesstr) {
						result.append("\t" + s);
					}
					result.append("\n");
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

				result.append("'" + toclassify.get(idx).toString() + "'\t" + "'" + sorted.get(0) + "'");

				for (String s : classlablesstr) {
					result.append("\t" + scores.get(s));
				}
				result.append("\n");

				idx++;
			}
		}
		if (opt.getOutputscvfile()!=null) {
			FileWriter fw = new FileWriter(opt.getOutputscvfile());
			fw.write(result.toString());
			fw.close();
		}
		System.out.println(result.toString());
	}

	private Options getopt(String[] argv) {
		Options opt=new Options();
		int c;
		String arg;
		LongOpt[] longopts = new LongOpt[5];
		//
		StringBuffer outputcsv = new StringBuffer();
		longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
		longopts[1] = new LongOpt("outputcsv", LongOpt.OPTIONAL_ARGUMENT, outputcsv, 'o');
		StringBuffer modeldir = new StringBuffer();
		StringBuffer inputdir = new StringBuffer();
		longopts[2] = new LongOpt("modeldir", LongOpt.OPTIONAL_ARGUMENT, modeldir, 'm');
		longopts[3] = new LongOpt("inputdir", LongOpt.REQUIRED_ARGUMENT, inputdir, 'i');
		longopts[4] = new LongOpt("classattribute", LongOpt.REQUIRED_ARGUMENT, inputdir, 'c');
		//
		Getopt g = new Getopt("issclassifier", argv, "", longopts);
		//g.setOpterr(false); // We'll do our own error handling
		//
		
		while ((c = g.getopt()) != -1)
			switch (c) {
			case 0:
				arg = g.getOptarg();
				int index = g.getLongind();
				switch(index)
				{
				case 1:{
					opt.setOutputscvfile(new File(arg));
				}break;
				case 2:{
					opt.setModeldir(arg);
				}break;
				case 3:{
					opt.setImagedir(arg);
				}break;
				case 4:{
					opt.setClassattribute(arg);
				}break;
				
				
				}
			//	System.out.println("ISSClassifier can classify photographs from the International Space Station into: city, aurora, astronaut and stars");
			
				break;
			case 1:
		          System.out.println("I see you have return in order set and that " +
		                             "a non-option argv element was just found " +
		                             "with the value '" + g.getOptarg() + "'");
		          break;
		          //
		        case 2:
		          arg = g.getOptarg();
		          System.out.println("I know this, but pretend I didn't");
		          System.out.println("We picked option " +
		                             longopts[g.getLongind()].getName() +
		                           " with value " + 
		                           ((arg != null) ? arg : "null"));
		          break;
		          //
		        case 'b':
		          System.out.println("You picked plain old option " + (char)c);
		          break;
		          //
		        case 'c':
		        case 'd':
		          arg = g.getOptarg();
		          System.out.println("You picked option '" + (char)c + 
		                             "' with argument " +
		                             ((arg != null) ? arg : "null"));
		          break;
		          //
		        case 'h':
		          System.out.println("I see you asked for help");
		          break;
		          //
		        case 'W':
		          System.out.println("Hmmm. You tried a -W with an incorrect long " +
		                             "option name");
		          break;
		          //
		        case ':':
		          System.out.println("Doh! You need an argument for option " +
		                             (char)g.getOptopt());
		          break;
		          //
		        case '?':
		          System.out.println("The option '" + (char)g.getOptopt() + 
		                           "' is not valid");
		          break;
		          //
		        default:
		          System.out.println("getopt() returned " + c);
		          break;
			}
		//
		for (int i = g.getOptind(); i < argv.length; i++)
			System.out.println("Non option argv element: " + argv[i] + "\n");
		

		return opt;
	}
	
	class Options
	{
		String modeldir;
		String imagedir;
		String classattribute;
		File outputscvfile;
		boolean valid=true;
		public String getModeldir() {
			return modeldir;
		}
		public void setModeldir(String modeldir) {
			this.modeldir = modeldir;
		}
		public String getImagedir() {
			return imagedir;
		}
		public void setImagedir(String imagedir) {
			this.imagedir = imagedir;
		}
		public String getClassattribute() {
			return classattribute;
		}
		public void setClassattribute(String classattribute) {
			this.classattribute = classattribute;
		}
		public File getOutputscvfile() {
			return outputscvfile;
		}
		public void setOutputscvfile(File outputscvfile) {
			this.outputscvfile = outputscvfile;
		}

		boolean isValid()
		{
			return !(getClassattribute()==null|| getImagedir()==null);
		}
		
		
	}
}
