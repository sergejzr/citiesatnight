package sz.de.l3s.features;

import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Set;

public interface FeatureExtractor {

	/**
	 * 
	 * @param image
	 * @return pairs: <feature name, textdescription> Example:
	 *         <"haarfaces-frontalface_alt-count",
	 *         "uk.ac.soton.ecs.jsh2.feature.IntFV 1 1\n0">
	 */
	Hashtable<String, String> extractFrom(BufferedImage image, Set<String> features) throws Exception;

	public Set<String> getAvailableFeatures();

}
