package features;



import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.imageio.ImageIO;

import com.thoughtworks.xstream.io.binary.Token.Attribute;

import sz.de.l3s.features.FeatureReader;
import sz.de.l3s.features.VisualFeature;
import sz.de.l3s.features.VisualFeatureGroup;
import uk.ac.soton.ecs.jsh2.picalert.ImageFeatureExtractor;
import weka.core.Instances;

public class Image2Features {

	ImageFeatureExtractor fe;

	public Image2Features() {
		fe = new ImageFeatureExtractor();
	}

	public static void main(String[] args) {

		Image2Features ife = new Image2Features();
		File arffile = new File(args[0]);
		File imag = new File(args[1]);
		File arffout = null;
		if (args.length > 2) {
			arffout = new File(args[2]);
		}

		String sb=null;
		try {
			sb = ife.toArff(arffile, imag, arffout);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (arffout == null) {
			System.out.println(sb);
		}
	}

	public String toArff(File arffilein, File imag, File arffileout) throws IOException {
		FileReader fr=null;
		
		FileInputStream fis=new FileInputStream(arffilein);
		String res = toArff(arffilein, imag, arffileout);
		fis.close();
		return res;
		
}
	public String toArff(InputStream arffstream, String imag, File arffileout) throws IOException {

		ArrayList<File> filestoclassify = new ArrayList<>();
File imagefile=null;
		URL toreadfrom=null;
		if (imag.startsWith("http://"))
		{
			toreadfrom=new URL(imag);
		}else{
			imagefile=new File(imag);
		if (imagefile.isDirectory()) {
			filestoclassify.addAll(Arrays.asList(imagefile.listFiles()));
		} else {
			filestoclassify.add(imagefile);
		}
		}

		
			BufferedReader reader = new BufferedReader(new InputStreamReader(arffstream));
			Instances data = new Instances(reader);
			StringBuilder sb = createHeader(data);
			
			
			if(toreadfrom==null)
			for (File cur : filestoclassify) {
				BufferedImage test=null;
				try {
					test = ImageIO.read(cur);
				} catch (Exception e) {
					log("Photo could not be parsed imageIO: " + cur);
					// e.printStackTrace();
					
				}
				if (test == null) {
					log("Photo could not be parsed Unknownerror: " + cur);
				}
				sb.append(extractfromSource(test,data));
			}else
			{BufferedImage test=null;
			try {
				test = ImageIO.read(toreadfrom);
			} catch (Exception e) {
				log("Photo could not be parsed imageIO: " + toreadfrom);
				// e.printStackTrace();
				
			}
			if (test == null) {
				log("Photo could not be parsed Unknownerror: " + toreadfrom);
			}
				sb.append(extractfromSource(test,data));
			}

			
			
			if (arffileout != null) {
				FileWriter fw = new FileWriter(arffileout);
				fw.write(sb.toString());

				fw.close();
			}
			return sb.toString();

		
		
	}
	
	
	public String toArff(InputStream arffstream, String images[], File arffileout) throws IOException {



		
			BufferedReader reader = new BufferedReader(new InputStreamReader(arffstream));
			Instances data = new Instances(reader);
			StringBuilder sb = createHeader(data);
			
		for	(String imag: images){
			
			ArrayList<File> filestoclassify = new ArrayList<>();
			File imagefile=null;
					URL toreadfrom=null;
					if (imag.startsWith("http://"))
					{
						toreadfrom=new URL(imag);
					}else{
						imagefile=new File(imag);
					if (imagefile.isDirectory()) {
						filestoclassify.addAll(Arrays.asList(imagefile.listFiles()));
					} else {
						filestoclassify.add(imagefile);
					}
					}
			if(toreadfrom==null)
			for (File cur : filestoclassify) {
				BufferedImage test=null;
				try {
					test = ImageIO.read(cur);
				} catch (Exception e) {
					log("Photo could not be parsed imageIO: " + cur);
					// e.printStackTrace();
					
				}
				if (test == null) {
					log("Photo could not be parsed Unknownerror: " + cur);
				}
				sb.append(extractfromSource(test,data));
			}else
			{BufferedImage test=null;
			try {
				test = ImageIO.read(toreadfrom);
			} catch (Exception e) {
				log("Photo could not be parsed imageIO: " + toreadfrom);
				// e.printStackTrace();
				
			}
			if (test == null) {
				log("Photo could not be parsed Unknownerror: " + toreadfrom);
			}
				sb.append(extractfromSource(test,data));
			}
		}
			
			
			if (arffileout != null) {
				FileWriter fw = new FileWriter(arffileout);
				fw.write(sb.toString());

				fw.close();
			}
			return sb.toString();

		
		
	}
	
	private String extractfromSource(BufferedImage test, Instances data) {
StringBuilder ret=new StringBuilder();
		

		Hashtable<String, String> fts = fe.extractFrom(test);
		FeatureReader fr = new FeatureReader();

		Map<String, VisualFeatureGroup> vfeatures = fr.readFeatures("newimg", fts);

		ret.append("{");

		Hashtable<String, Double> aggregated = new Hashtable<>();

		for (String h : vfeatures.keySet()) {
			Map<? extends String, ? extends VisualFeature> fconti = vfeatures.get(h).getFeatures();
			for (VisualFeature f : fconti.values()) {
				aggregated.put(f.getFeaturename(), f.getValue());
			}
		}

		StringBuilder line = new StringBuilder();
		for (int attribute = 0; attribute < data.numAttributes() - 1; attribute++) {

			Double val = aggregated.get(data.attribute(attribute).name());
			if (val == null) {
				continue;
			}
			if (line.length() > 0)
				line.append(",");
			line.append(attribute + " " + val);
		}
		ret.append(line);
		ret.append("}\n");
return ret.toString();
	}

	private StringBuilder createHeader(Instances data) {
		StringBuilder sb = new StringBuilder();

		sb.append("@RELATION ISSIMages\n\n");

		for (int attribute = 0; attribute < data.numAttributes() - 1; attribute++) {

			sb.append(
					"@ATTRIBUTE " + data.attribute(attribute).name() + " " + getType(data.attribute(attribute)) + "\n");
		}

		sb.append("@DATA\n\n");

		return sb;
	}

	Hashtable<Integer, String> types = new Hashtable<>();

	private String getType(weka.core.Attribute attribute) {
		String type = types.get(attribute.index());
		if (type != null)
			return type;

		if (attribute.isNominal()) {
			List<String> values = new ArrayList<>();

			Enumeration aenum = attribute.enumerateValues();
			while (aenum.hasMoreElements()) {
				values.add(aenum.nextElement().toString());
			}

			StringBuilder sb = new StringBuilder();
			for (String s : values) {
				if (sb.length() > 0)
					sb.append(",");
				sb.append(s);
			}
			types.put(attribute.index(), "{" + sb.toString() + "}");
			return "{" + sb.toString() + "}";
		} else {
			String atype = null;
			if (attribute.isNumeric()) {
				atype = "numeric";
			}

			if (attribute.isString()) {
				atype = "string";
			}
			types.put(attribute.index(), atype);
			return atype;
		}

	}

	private Object log(String string) {
		System.out.println(string);
		return null;
	}
}
