package uk.ac.soton.ecs.jsh2.picalert;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analysis.algorithm.EdgeDirectionCoherenceVector;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.global.AvgBrightness;
import org.openimaj.image.feature.global.HueStats;
import org.openimaj.image.feature.global.Naturalness;
import org.openimaj.image.feature.global.Sharpness;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.pixel.statistics.BlockHistogramModel;
import org.openimaj.image.pixel.statistics.HistogramModel;
//import org.openimaj.image.processing.algorithm.EdgeDirectionCoherenceVector;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetectorFeatures;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.util.pair.IntFloatPair;

import sz.de.l3s.features.FeatureExtractor;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 * small updates by s.zerr@soton.ac.uk
 * 
 */
public class ImageFeatureExtractor implements FeatureExtractor {
	public ImageFeatureExtractor() {
		for (int i = 0; i < CASCADES.length; i++)
			HAAR_DETECTORS[i] = new HaarCascadeDetector("haarcascade_" + CASCADES[i] + ".xml", 80);

	}

	private static final int MAX_IMAGE_SIZE = 640;

	// Loading and optimising a quantiser takes some time, but only needs to be
	// done once
	protected static final String[] QUANTISERS = { "12", // "6", "3"
	}; // EDIT THIS TO REMOVE SIFT FEATURES
	protected static final ByteCentroidsResult[] QUANT = new ByteCentroidsResult[QUANTISERS.length];

	static {
		for (int i = 0; i < QUANTISERS.length; i++)
			QUANT[i] = loadQuantiser("pubpriv-dog-sift-fkm" + QUANTISERS[i] + "K-rnd1M.byte");
	}

	// same goes for the Haar cascades
	protected static final String[] CASCADES = { "frontalface_alt",
			// "frontalface_alt2", "frontalface_default", "fullbody",
			"profileface",
			// "upperbody"
	}; // EDIT THIS TO REMOVE FACE/BODY FEATURES
	HaarCascadeDetector[] HAAR_DETECTORS = new HaarCascadeDetector[CASCADES.length];

	/**
	 * load a quantiser resource for the sift features
	 * 
	 * @param name
	 *            name of the resource
	 * @return
	 */
	protected static ByteCentroidsResult loadQuantiser(String name) {
		try {
			 InputStream bis = ImageFeatureExtractor.class.getResourceAsStream(name);

			 byte[][] c=deserialize(bis, byte[][].class);
			 
			ByteCentroidsResult quant = new ByteCentroidsResult();
			quant.centroids=c;
			
			
			//IOUtils.read(bis, ByteCentroidsResult.class);
					
					//deserialize(bis, ByteCentroidsResult.class);//IOUtils.read(bis, ByteCentroidsResult.class);

			// byte[][] clusters = quant.getClusters();
			return quant;
		} catch (IOException e) {
			e.printStackTrace();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}

	/**
	 * Compute the quantised sift features for a given quantiser
	 * 
	 * @param keys
	 * @param quant
	 * @return
	 */
	protected String computeQuantisedSIFTFeature(LocalFeatureList<Keypoint> keys, ByteCentroidsResult quant) {
		StringBuffer sb = new StringBuffer();

		sb.append(keys.size() + "\n" + quant.numClusters() + "\n");

		for (Keypoint k : keys) {

			HardAssigner<byte[], float[], IntFloatPair> assigner = quant.defaultHardAssigner();

			int id = assigner.assign(k.getFeatureVector().getVector());

			sb.append(String.format("%4.2f %4.2f %4.2f %4.3f %d\n", k.y, k.x, k.scale, k.ori, id));
		}

		return sb.toString();
	}

	/**
	 * Resize the image if it is too big, otherwise just return it
	 * 
	 * @param image
	 *            the image to resize
	 * @param maxSide
	 *            the maximum allowed dimension
	 * @return the scaled image
	 */
	protected MBFImage resizeMax(MBFImage image, int maxSide) {
		int actualMax = Math.max(image.getHeight(), image.getWidth());

		if (actualMax < maxSide)
			return image;

		float scale = (float) maxSide / (float) actualMax;
		ResizeProcessor rp = new ResizeProcessor(scale);

		return image.processInplace(rp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.l3s.l3sws.picalert.FeatureExtractor#extractFrom(java.awt.image.
	 * BufferedImage)
	 */

	public Hashtable<String, String> extractFrom(BufferedImage image) {
		Hashtable<String, String> res = new Hashtable<String, String>();
		try {
			res = extractFrom(image, getAvailableFeatures());
		} catch (Exception e) {
			this.e = e;
			e.printStackTrace();
		}
		return res;
	}

	public Set<String> getAvailableFeatures() {
		HashSet<String> ret = new HashSet<String>();
		// TODO Auto-generated method stub
		String[] st = new String[] { "dog-sift-fkm12k-rnd1M",

				"edch",

				"haarfaces-frontalface_alt-area",

				"haarfaces-profileface-area", "globalhist_hsv_444", "avg_brightness", "naturalness", "sharpness",
				"hue_stats", "colorfulness"

		};
		for (String s : st) {
			ret.add(s);
		}
		return ret;
	}

	/**
	 * Convert a {@link FeatureVector} provided by a
	 * {@link FeatureVectorProvider} to the same form as it would be if written
	 * to a file in ASCII format
	 * 
	 * @param fvp
	 *            the {@link FeatureVectorProvider}
	 * @return the string encoding the features
	 */
	protected String fvProviderToString(FeatureVectorProvider<?> fvp) {
		return fvToString(fvp.getFeatureVector());
	}

	/**
	 * Convert a {@link FeatureVector} to the same form as it would be if
	 * written to a file in ASCII format
	 * 
	 * @param fv
	 *            the {@link FeatureVector}
	 * @return the string encoding the features
	 */
	protected String fvToString(FeatureVector fv) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.writeASCII(baos, fv);
			return baos.toString("UTF-8");
		} catch (IOException e) {
			return "";
		}
	}

	/**
	 * Just for testing
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		testaPic();
	}

	public static void testaPic() {

		// BufferedImage test = ImageIO.read(new
		// File("E:\\webservicefolder\\image\\flickr\\008\\284\\233\\821\\008284233821.jpg"));

		try {
			String[] fotos = new String[] {
					// "/data2/zerr/taggedimages_clean/002/167/644/838/002167644838.jpg",
					// "/data2/zerr/taggedimages_clean/000/424/785/153/000424785153.jpg"
					"/media/zerr/BA0E0E3E0E0DF3E3/darkskiesimgs/000/000/0IS/S04/5-E/-97/032/0000000ISS045-E-97032.jpg" };
			ImageFeatureExtractor fe = new ImageFeatureExtractor();
			for (String f : fotos) {

				BufferedImage test = ImageIO.read(new File(f));
				HashSet<String> hs = new HashSet<String>();
			
				hs.add("dog-sift-fkm12k-rnd1M");
				Hashtable<String, String> res = fe.extractFrom(test, hs);

				Set<String> features = fe.getAvailableFeatures();
				// hs.addAll(res.keySet());

				if (false && res.size() != features.size()) {
					System.out.println(f);
					fe.getException().printStackTrace();
				} else {
					System.out.println(res);
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	Exception e = null;

	public Hashtable<String, String> extractFrom(BufferedImage image, Set<String> features) throws Exception {
		Hashtable<String, String> data = new Hashtable<String, String>();

		e = null;

		// pre-calculate the image representations we'll need
		// we'll also limit the size of provided images to a maximum
		MBFImage rgbimg = null;// resizeMax(ImageUtilities.createMBFImage(image,
								// false), MAX_IMAGE_SIZE); //RGB

		try {
			if (image == null) {
				System.out.println("img==null");
			}
			rgbimg = resizeMax(ImageUtilities.createMBFImage(image, false), MAX_IMAGE_SIZE); // RGB
		} catch (Exception e) {
			e.printStackTrace();
			return data;
		}

		MBFImage hsvimg = Transforms.RGB_TO_HSV(rgbimg); // HSV

		FImage greyimg = Transforms.calculateIntensityNTSC(rgbimg); // grey

		for (String f : features) {

			if (f.startsWith("dog-sift-")) {
				// sift
				if (QUANTISERS.length > 0) {
					LocalFeatureList<Keypoint> keys = new DoGSIFTEngine().findFeatures(greyimg);
					for (int i = 0; i < QUANTISERS.length; i++) {
						data.put("dog-sift-fkm" + QUANTISERS[i] + "k-rnd1M",
								computeQuantisedSIFTFeature(keys, QUANT[i]));
					}

				}
			}

			if (f.equals("avg_brightness")) {
				// avg_brightness
				AvgBrightness avgBrightness = new AvgBrightness();
				avgBrightness.analyseImage(rgbimg);
				data.put("avg_brightness", fvProviderToString(avgBrightness));

			}
			if (f.equals(
					"colorfulness")) {/*
										 * // colorfulness, colorfulness_classes
										 * Colorfulness colorfulness = new
										 * Colorfulness();
										 * 
										 * rgbimg.process(colorfulness);
										 * data.put("colorfulness",
										 * fvProviderToString(colorfulness)); //
										 * data.put("colorfulness_classes", //
										 * fvProviderToString(colorfulness.
										 * getColorfulnessAttribute()));
										 * 
										 */
			}
			if (f.equals("edch")) {
				// edch
				EdgeDirectionCoherenceVector edcv = new EdgeDirectionCoherenceVector();
				edcv.analyseImage(greyimg);

				data.put("edch", fvProviderToString(edcv));
			}

			if (f.equals("globalhist_hsv_444")) {
				// GLOBAL HISTOGRAMS
				HistogramModel histogram444 = new HistogramModel(4, 4, 4); // NOTE
																			// :
																			// shared
																			// between
																			// hsv
																			// &
																			// rgb

				// globalhist_hsv_444
				histogram444.estimateModel(hsvimg);
				data.put("globalhist_hsv_444", fvProviderToString(histogram444));
			}

			/*
			 * //globalhist_rgb_444 histogram444.estimateModel(rgbimg);
			 * data.put("globalhist_rgb_444", fvProviderToString(histogram444));
			 */
			if (f.equals("localhist_hsv_44_444")) {
				// LOCAL HISTOGRAMS
				BlockHistogramModel localHistogram444_44 = new BlockHistogramModel(4, 4, 4, 4, 4); // NOTE
																									// :
																									// shared
																									// between
																									// hsv
																									// &
																									// rgb

				// localhist_hsv_44_444
				localHistogram444_44.estimateModel(hsvimg);
				data.put("localhist_hsv_44_444", fvProviderToString(localHistogram444_44));
			}
			/*
			 * //localhist_rgb_44_444
			 * localHistogram444_44.estimateModel(rgbimg);
			 * data.put("localhist_rgb_44_444",
			 * fvProviderToString(localHistogram444_44));
			 */
			if (f.equals("hue_stats")) {
				// hue_stats
				HueStats hueStats = new HueStats();
				hueStats.analyseImage(hsvimg);

				data.put("hue_stats", fvProviderToString(hueStats));
			}
			if (f.equals("naturalness")) {
				// naturalness
				Naturalness naturalness = new Naturalness();
				naturalness.analyseImage(rgbimg);

				data.put("naturalness", fvProviderToString(naturalness));
			}
			if (f.equals("sharpness")) {
				// sharpness
				Sharpness sharpness = new Sharpness();
				sharpness.analyseImage(greyimg);

				data.put("sharpness", fvProviderToString(sharpness));
			}
			if (f.startsWith("haarfaces-")) {
				try {
					getFaceFeatures(f, greyimg, data);
				} catch (Exception e) {
					// TODO: check this extractor
					this.e = e;
					// e.printStackTrace();
					// throw e;
				}
			}

		}
		return data;
	}

	private synchronized void getFaceFeatures(String f, FImage greyimg, Hashtable<String, String> data) {
		// haar cascade features (area, box, count)
		for (int i = 0; i < CASCADES.length; i++) {
			if (f.equals("haarfaces-" + CASCADES[i] + "-area")) {
				List<DetectedFace> faces = null;
				try {
					faces = HAAR_DETECTORS[i].detectFaces(greyimg);
				} catch (Exception e) {
					i--;
					continue;
				}
				data.put("haarfaces-" + CASCADES[i] + "-area",
						fvToString(FaceDetectorFeatures.AREA.getFeatureVector(faces, greyimg)));
				// data.put("haarfaces-"+CASCADES[i]+"-box",
				// fvToString(FaceDetectorFeatures.BOX.getFeatureVector(faces,
				// greyimg)));
				// data.put("haarfaces-"+CASCADES[i]+"-count",
				// fvToString(FaceDetectorFeatures.COUNT.getFeatureVector(faces,
				// greyimg)));
			}
		}

	}

	public Exception getException() {
		return e;

	}
	public static void serialize(File vocfile, Object vocabulary) throws IOException {

		FileOutputStream fos = new FileOutputStream(vocfile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(vocabulary);
		oos.flush();
		oos.close();
	}


	public  static <T> T deserialize(InputStream fis,Class<T> class1) throws IOException,
			ClassNotFoundException {

		// = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Object myDeserializedObject = ois.readObject();
		ois.close();
		return class1.cast(myDeserializedObject);

	}
}
