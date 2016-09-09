package de.l3s.image;



import java.util.Hashtable;
import java.util.Map;

import sz.de.l3s.features.FeatureReader;
import sz.de.l3s.features.VisualFeatureGroup;

public class FeatureConverter {
	FeatureReader fr = new FeatureReader();

	public FeatureConverter() {
		// TODO Auto-generated constructor stub
	}

	public Map<String, VisualFeatureGroup> convertToFeatures(String id,
			Hashtable<String, String> featuredescriptions) {

		Map<String, VisualFeatureGroup> ret = new Hashtable<String, VisualFeatureGroup>();
		Map<String, VisualFeatureGroup> ft = fr.readFeatures(id,
				featuredescriptions);
		ret.putAll(ft);
		return ret;

	}

}
