package sz.de.l3s.features;

import java.util.Vector;

public class VisualFeature {

	private String featurename;
	Vector<String> infos =new Vector<String>();
	private Double value;

	public VisualFeature(String featurename, Double value) {
		this.featurename = featurename;
		this.value = value;
	}

	public void addFeatureInfo(String info) {
		if(infos==null)
		{
			infos= new Vector<String>();
		}
		infos.add(info);
	}

	@Override
	public String toString() {
		return featurename + ":" + value;
	}

	public String getFeaturename() {
		return featurename;
	}

	public void setFeaturename(String featurename) {
		this.featurename = featurename;
	}

	public Vector<String> getInfos() {
		return infos;
	}

	public void setInfos(Vector<String> infos) {
		this.infos = infos;
	}

	public int size() {
		return infos.size();
	}

	public Double getValue() {

		return value;
	}

	public void setValue(Double value) {
		this.value = value;

	}

}
