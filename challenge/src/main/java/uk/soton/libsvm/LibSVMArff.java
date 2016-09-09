package uk.soton.libsvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import weka.core.Attribute;
import weka.core.Instances;



public class LibSVMArff {

	private File dir;
	private String modelname;
	private svm_model model;
	private svm_parameter param;
	private svm_problem prob;
	Logger logger = LogManager.getLogger(LibSVMArff.class);
	
	private Instances data;
	private TreeBidiMap<String, Integer> indexidx;
	private TreeBidiMap<String, Double> classlables;
	String error_msg;
	private String classattribute;
	
	public LibSVMArff(InputStream arffstream, InputStream modelstream, String classattribute) throws IOException
	{
	
		this.classattribute=classattribute;
		param = new svm_parameter();
		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.LINEAR;
		param.degree = 3;
		param.gamma = 0; // 1/num_features
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 1;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];
		
		
		InputStream bis = LibSVMArff.class
				.getResourceAsStream("");
		

		
		model = svm.svm_load_model(new BufferedReader(new InputStreamReader(modelstream)));
		
	
				if (svm.svm_check_probability_model(model) == 0) {
					System.err.print("Model does not support probabiliy estimates\n");

				} else if (svm.svm_check_probability_model(model) != 0) {
					//logger.info("Model supports probability estimates.\n");
				}
			
		
		
		
		
	
		BufferedReader reader;
		reader = new BufferedReader(new InputStreamReader(arffstream));
		data = new Instances(reader);
		
		this.indexidx = new TreeBidiMap<String, Integer>();
		
		for (int i = 0; i < data.numAttributes(); i++) {
			this.indexidx.put(data.attribute(i).name(), i);
		}
		
		Attribute classattr =data.attribute(classattribute);
	
		Enumeration vals = classattr.enumerateValues();
		Double classidx=1.0;
		classlables=new TreeBidiMap<>();
		while (vals.hasMoreElements()) {
			classlables.put(vals.nextElement().toString(), classidx++);
		}
	
		
	}
	public LibSVMArff(File arff, Properties props, File modeloutputdir, String modelname, String classattribute) throws IOException
	{
		this.dir=modeloutputdir;
		this.modelname=modelname;
		this.classattribute=classattribute;
		param = new svm_parameter();
		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.LINEAR;
		param.degree = 3;
		param.gamma = 0; // 1/num_features
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 1;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];
		
		File modelfile = new File(new File(dir, modelname), modelname + ".svmlighmodel");
		if (modelfile.exists()) {
			try {
				model = svm.svm_load_model(modelfile.getAbsolutePath());
				if (svm.svm_check_probability_model(model) == 0) {
					System.err.print("Model does not support probabiliy estimates\n");

				} else if (svm.svm_check_probability_model(model) != 0) {
					//logger.info("Model supports probability estimates.\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
	
		BufferedReader reader;
		reader = new BufferedReader(new FileReader(arff));
		data = new Instances(reader);
		
		this.indexidx = new TreeBidiMap<String, Integer>();
		
		for (int i = 0; i < data.numAttributes(); i++) {
			this.indexidx.put(data.attribute(i).name(), i);
		}
		
		Attribute classattr =data.attribute(classattribute);
	
		Enumeration vals = classattr.enumerateValues();
		Double classidx=1.0;
		classlables=new TreeBidiMap<>();
		while (vals.hasMoreElements()) {
			classlables.put(vals.nextElement().toString(), classidx++);
		}
	}
	
	public void trainFromArff()
	{
		prob = new svm_problem();
		prob.l = data.numInstances();
		prob.x = new svm_node[prob.l][];
		prob.y = new double[prob.l];
int classidx=-1;

		for (int instance = 0; instance < data.numInstances(); instance++) {
			HashMap<String, Double> example = new HashMap<String, Double>(); //
			/* String prepared_String = "";// */
			String strlbel = classattribute;

			List<svm_node> nodes = new ArrayList<svm_node>();
			
			
			for (int attribute = 0; attribute < data.numAttributes() - 1; attribute++) {
				if(!data.attribute(attribute).name().equals(strlbel)){
					svm_node node;
					nodes.add(node = new svm_node());
					node.index = attribute;
					node.value = data.instance(instance).value(attribute);
				}else
				{
					classidx=attribute;
				}
			}

			svm_node[] node=new svm_node[nodes.size()];
			nodes.toArray(node);
				prob.x[instance] =node;
				prob.y[instance] = classlables.get(data.instance(instance).stringValue(classidx));
		}

	
		if (param.gamma == 0 && data.numAttributes() > 0)
			param.gamma = 1.0 / data.numAttributes();

		if (param.kernel_type == svm_parameter.PRECOMPUTED)
			for (int i = 0; i < prob.l; i++) {
				if (prob.x[i][0].index != 0) {
					System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
					System.exit(1);
				}
				if ((int) prob.x[i][0].value <= 0 || (int) prob.x[i][0].value > data.numAttributes()) {
					System.err.print("Wrong input format: sample_serial_number out of range\n");
					System.exit(1);
				}
			}
		error_msg = svm.svm_check_parameter(prob, param);

		if (error_msg != null) {
			System.err.print("ERROR: " + error_msg + "\n");
			System.exit(1);
		}
		model = svm.svm_train(prob, param);
	}
	
	
public void classifyArff(File arff) throws IOException 
{
	BufferedReader reader;

	reader = new BufferedReader(new FileReader(arff));
	Instances data = new Instances(reader);
	classifyData(data);


}
	
private void classifyData(Instances data) {
	int predict_probability=1;
	
	
	
	Attribute classattr =data.attribute(classattribute);	

	 TreeBidiMap<String, Double> classlables=new TreeBidiMap<>();
	Enumeration vals = classattr.enumerateValues();
	Double classidx=1.0;

while (vals.hasMoreElements()) {
classlables.put(vals.nextElement().toString(), classidx++);
}

Hashtable<String, Hashtable<String, Integer>> tester=new Hashtable<String, Hashtable<String,Integer>>();
int trueclass=0,wrongclass=0;





int correct = 0;
int total = 0;
double error = 0;
double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

int svm_type = svm.svm_get_svm_type(model);
int nr_class = svm.svm_get_nr_class(model);

int[] intlabels = new int[nr_class];
svm.svm_get_labels(model, intlabels);

double[] prob_estimates = null;
double dlabels[] = null;
if (predict_probability == 1) {
if (svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
	logger.info(
			"Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="
					+ svm.svm_get_svr_probability(model) + "\n");
} else {
	dlabels = new double[nr_class];
	int[] labels = new int[nr_class];
	svm.svm_get_labels(model, labels);
	prob_estimates = new double[nr_class];
	for (int i = 0; i < prob_estimates.length; i++) {
		dlabels[i] = labels[i];
	}

	/*
	 * output.writeBytes("labels"); for(int j=0;j<nr_class;j++)
	 * output.writeBytes(" "+labels[j]); output.writeBytes("\n");
	 */
}
}












for (int instance = 0; instance < data.numInstances(); instance++) {


ClassificationResult result = null;
List<svm_node> lexample = new ArrayList<svm_node>();

for (int attribute = 0; attribute < data.numAttributes() - 1; attribute++) {
if(!data.attribute(attribute).name().equals(classattr)){
	
	svm_node node;
	lexample.add(node = new svm_node());
	node.index = attribute;
	node.value =  data.instance(instance).value(attribute);
	
	}
}
svm_node[] x = lexample.toArray(new svm_node[lexample.size()]);
double v;


if (predict_probability == 1 && (svm_type == svm_parameter.C_SVC || svm_type == svm_parameter.NU_SVC)) {
v = svm.svm_predict_probability(model, x, prob_estimates);

result = new ClassificationResult(dlabels, v, prob_estimates);

} else {
v = svm.svm_predict(model, x);
result = new ClassificationResult((int) v);
}
String classstr=data.instance(instance).stringValue(classattr);

Hashtable<String, Integer> forclasstest = tester.get(classstr);
if(forclasstest==null) {tester.put(classstr, forclasstest=new Hashtable<String, Integer>()); forclasstest.put("true", 0); forclasstest.put("wrong", 0);}


String classlab=classlables.getKey(result.getLable());

if(classstr.equals(classlab)){
 
 forclasstest.put("true",forclasstest.get("true")+1);
 } else{ forclasstest.put("wrong",forclasstest.get("wrong")+1);}

System.out.print(" Instance is a "+classstr+ " and was classified as "+classlab+" other probabilities: [");

for(String lab:classlables.keySet())
{
result.getLable();	

try {
		
	System.out.print(lab+":"+result.getProbabilityForClass(classlables.get(lab))+",");

} catch (Exception e) {
// TODO Auto-generated catch block
e.printStackTrace();
}
System.out.println("]");
}

}
System.out.println(tester);
}
private List<ClassificationResult> classifyInstance(Instances data) {
	List<ClassificationResult> ret=new ArrayList<>();

	int predict_probability=1;
	
	
	
	Attribute classattr =data.attribute(classattribute);	

	 TreeBidiMap<String, Double> classlables=new TreeBidiMap<>();
	Enumeration vals = classattr.enumerateValues();
	Double classidx=1.0;

while (vals.hasMoreElements()) {
classlables.put(vals.nextElement().toString(), classidx++);
}







int correct = 0;
int total = 0;
double error = 0;
double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

int svm_type = svm.svm_get_svm_type(model);
int nr_class = svm.svm_get_nr_class(model);

int[] intlabels = new int[nr_class];
svm.svm_get_labels(model, intlabels);

double[] prob_estimates = null;
double dlabels[] = null;
if (predict_probability == 1) {
if (svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
	logger.info(
			"Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="
					+ svm.svm_get_svr_probability(model) + "\n");
} else {
	dlabels = new double[nr_class];
	int[] labels = new int[nr_class];
	svm.svm_get_labels(model, labels);
	prob_estimates = new double[nr_class];
	for (int i = 0; i < prob_estimates.length; i++) {
		dlabels[i] = labels[i];
	}

	/*
	 * output.writeBytes("labels"); for(int j=0;j<nr_class;j++)
	 * output.writeBytes(" "+labels[j]); output.writeBytes("\n");
	 */
}
}












for (int instance = 0; instance < data.numInstances(); instance++) {


ClassificationResult result = null;
List<svm_node> lexample = new ArrayList<svm_node>();

for (int attribute = 0; attribute < this.data.numAttributes() - 1; attribute++) {
	
	
if(!this.data.attribute(attribute).name().equals(classattr)){
	
	Attribute dataattribute = data.attribute(this.data.attribute(attribute).name());
	if(dataattribute==null){ continue;}
	svm_node node;
	lexample.add(node = new svm_node());
	node.index = attribute;
	node.value =  data.instance(instance).value(dataattribute);
	
	}
}
svm_node[] x = lexample.toArray(new svm_node[lexample.size()]);
double v;


if (predict_probability == 1 && (svm_type == svm_parameter.C_SVC || svm_type == svm_parameter.NU_SVC)) {
	
	
v = svm.svm_predict_probability(model, x, prob_estimates);

result = new ClassificationResult(dlabels, v, Arrays.copyOf(prob_estimates,prob_estimates.length));

} else {
v = svm.svm_predict(model, x);
result = new ClassificationResult((int) v);
}


result.setLabels(classlables);





ret.add(result);
}

return ret;
}
public void store() throws IOException {

	File modeldir = new File(dir, modelname);
	modeldir.mkdirs();
	File modelfile = new File(modeldir, modelname + ".svmlighmodel");
	svm.svm_save_model(modelfile.getAbsolutePath(), model);
	File indexfile = new File(modeldir, "idx_" + modelname + ".idx");

	try {
		FileOutputStream fos = new FileOutputStream(indexfile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(indexidx);
		oos.close();
		fos.close();
	} catch (IOException ex) {
		ex.printStackTrace();
	}

}

	public static void main(String[] args) {
		try {
			File arff;
			LibSVMArff ls = new LibSVMArff(arff=new File("/media/zerr/BA0E0E3E0E0DF3E3/darkskies/5_1_short_arff.arff"),null, new File("/media/zerr/BA0E0E3E0E0DF3E3/darkskies/testmodels"),"smallanddirty","labelclass");
			ls.trainFromArff();
			ls.classifyArff(arff);
			ls.store();
			LibSVMArff ls2 = new LibSVMArff(arff=new File("/media/zerr/BA0E0E3E0E0DF3E3/darkskies/5_1_short_arff.arff"),null, new File("/media/zerr/BA0E0E3E0E0DF3E3/darkskies/testmodels"),"smallanddirty","labelclass");
			System.out.println("\\reloaded");
			ls2.classifyArff(arff);
			System.out.println("reloaded\\");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			

	}

	public List<ClassificationResult> classifyInstance(String arffstring) {
		List<ClassificationResult> ret=new ArrayList<>();
		
	StringReader sr=new StringReader(arffstring);
	
	try {
		Instances newdata = new Instances(sr);
		 ret= classifyInstance(newdata);
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return ret;
		
	}
	
	
	
}
