package uk.soton.libsvm;

import java.util.Hashtable;

import org.apache.commons.collections4.bidimap.TreeBidiMap;

public class ClassificationResult {

	
	double[] prob_estimates;
	 double[] lables;
	private double lable;
	Hashtable<Double, Double> idx=null;
	private TreeBidiMap<String, Double> classlables;
	public double getProbabilityForClass(double classlable) throws Exception
	{
		if(prob_estimates==null)
		{
			throw new Exception("no probability estimates available");
		}
		if(idx==null)
		{
			idx=new Hashtable<Double, Double>();
			for(int i=0;i<lables.length;i++)
			{
				idx.put(lables[i], prob_estimates[i]);
			}
			
		}
		return idx.get(classlable);
	}
	public ClassificationResult(double lables[], double lable, double[] prob_estimates) {
		super();
	
		this.prob_estimates = prob_estimates;
		this.lable=lable;
		this.lables=lables;
	}
	public ClassificationResult(double lable) {
		super();
		
		this.lable=lable;
		
	}
	public double getLable() {
		return lable;
	}
@Override
public String toString() {
	StringBuilder sb=new StringBuilder();
	for(double prob:prob_estimates)
	{
		sb.append(prob+", ");
	}
	return "lable: "+lable+", probs: "+sb.toString();
}
public void setLabels(TreeBidiMap<String, Double> classlables) {
	this.classlables=classlables;
	
}
public TreeBidiMap<String, Double> getClasslables() {
	return classlables;
}
}
