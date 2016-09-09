package sz.de.l3s.features;

import java.util.HashMap;
import java.util.Map;

public class VisualFeatureGroup {

	private Map<String, VisualFeature> features;
	private String metadata;
	private String id;
	private String featurename;

	public String getMetadata() {
		return metadata;
	}

	public VisualFeatureGroup(String id, String featurename, String metadata) {
		this.id = id;
		this.metadata = metadata;
		features = new HashMap<String, VisualFeature>();
		this.featurename = featurename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.l3s.features.IVisualFeatureGroup#getFeatures()
	 */

	public Map<? extends String, ? extends VisualFeature> getFeatures() {
		return features;
	}

	public String getFeaturename() {
		return featurename;
	}

	public void setFeatures(HashMap<String, VisualFeature> features) {

		this.features = features;

	}

	public void addFeature(String featurename, String value) {

		features.put(featurename, new VisualFeature(featurename, Double.parseDouble(value)));
	}

	public String getId() {

		return id;
	}

}
