package de.l3s.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;



import sz.de.l3s.features.FeatureExtractor;
import sz.de.l3s.features.VisualFeature;
import sz.de.l3s.features.VisualFeatureGroup;
import sz.de.l3s.features.util.Features2SQLString;
import uk.ac.soton.ecs.jsh2.picalert.ImageFeatureExtractor;

public abstract class Picture {

	public String hasErrors() {
		return error;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public abstract String getStorageId();

	/*
	 * public Picture(String id, String pictureTitle, String pictureTags, String
	 * pictureDescription, String imageURL, String filename) {
	 * 
	 * super(); this.id = id; this.pictureTitle = pictureTitle; this.pictureTags
	 * = pictureTags; this.pictureDescription = pictureDescription;
	 * this.imageURL = imageURL; this.filename = filename; }
	 */
	FeatureConverter converter = new FeatureConverter();
	private File imgpath;
	protected String imagetype;
	private boolean hasvisualfeatures;

	public void storePicture(File directory) throws MalformedURLException, IOException {
		imgpath = directory;
		if (!exists(imgpath)) {
			BufferedImage img = downloadImage();
			File newimpath = getImagePathIn(new File(directory, imagetype));

			if (newimpath != null) {
				storePictureFromBytes(newimpath, img);
			}
		}
	}

	public abstract BufferedImage downloadImage() throws MalformedURLException, IOException;

	protected abstract File getImagePathIn(File directory);

	public BufferedImage readImage(File directory) throws IOException {
		File impath = getImagePathIn(new File(directory, imagetype));
		if (impath != null)
			return ImageIO.read(impath);
		return null;
	}

	private void storeFeatureDescriptions(File directory, Hashtable<String, String> featuredescriptions) {

		directory = getImagePathIn(new File(directory, getImageType())).getParentFile();
		try {

			for (String fname : featuredescriptions.keySet()) {
				FileWriter fw = new FileWriter(new File(directory, fname + ".fv"));

				String feature = featuredescriptions.get(fname);

				fw.write(feature);
				fw.flush();
				fw.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * protected void storeVisualFeatures(File directory, Map<String,
	 * VisualFeatureGroup> features) {
	 * 
	 * try {
	 * 
	 * for(String fname:features.keySet()) { FileWriter fw = new FileWriter(new
	 * File( directory, fname + ".fv"));
	 * 
	 * VisualFeatureGroup feature = features.get(fname);
	 * fw.write(feature.getMetadata()+"\n");
	 * 
	 * Map<? extends String, ? extends VisualFeature> fgroups =
	 * feature.getFeatures();
	 * 
	 * for(String s:fgroups.keySet()) {
	 * 
	 * }
	 * 
	 * fw.flush(); fw.close(); } } catch (IOException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } }
	 * 
	 */
	protected void storeVisualFeaturesSQLFile(File directory, Map<String, VisualFeatureGroup> features) {

		try {

			Hashtable<String, String> sqlfiles = Features2SQLString.toSQLString(getStorageId(), features);

			for (String fname : sqlfiles.keySet()) {

				FileWriter fw = new FileWriter(new File(directory, fname + ".ftr"));
				fw.write(sqlfiles.get(fname));
				fw.flush();
				fw.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Map<String, VisualFeatureGroup> getVisualFeatures(Set<String> usefeaturesidx,
			FeatureExtractor featureExtractor) throws IOException {

		if (getImagePathIn(new File(imgpath, getImageType())) == null) {
			return new HashMap<String, VisualFeatureGroup>();
		}
		return getVisualFeatures(imgpath, usefeaturesidx, featureExtractor);
	}

	protected Map<String, VisualFeatureGroup> getVisualFeatures(File imagedir, Set<String> usefeaturesidx,
			FeatureExtractor featureExtractor) throws IOException {

		Map<String, VisualFeatureGroup> ret;
		StringBuffer sb = new StringBuffer();

		File directory = getImagePathIn(new File(imagedir, getImageType())).getParentFile();
		Hashtable<String, String> featuredescriptions = new Hashtable<String, String>();
		for (File f : directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				return arg0.getName().endsWith(".fv");
			}
		})) {

			FileReader fr;
			try {
				// String
				// featurename=f.getName().substring(0,f.getName().indexOf("\\."));

				fr = new FileReader(f);
				BufferedReader br = new BufferedReader(fr);
				String line = null;

				while ((line = br.readLine()) != null) {
					if (sb.length() > 0)
						sb.append('\n');
					sb.append(line);
				}
				String featurename = f.getName().replace(".fv", "");

				featuredescriptions.put(featurename, sb.toString());
				sb = new StringBuffer();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// TODO:

		ret = converter.convertToFeatures(id, featuredescriptions);

		if (ret.size() > 0) {
			hasvisualfeatures = true;
		}
		HashSet<String> missingfeatures = new HashSet<String>();
		for (String k : usefeaturesidx) {
			if (!ret.containsKey(k)) {
				missingfeatures.add(k);
			}
		}
		if (missingfeatures.size() == 0) {
			visualFeatures = ret;
			return ret;
		}

		Map<String, VisualFeatureGroup> computedmissingfeatures = computeVisualFeatures(imagedir, missingfeatures,
				featureExtractor);

		if (computedmissingfeatures != null && computedmissingfeatures.size() > 0) {
			ret.putAll(computedmissingfeatures);
			// storeVisualFeatures(directory, computedmissingfeatures);
		}

		return visualFeatures = ret;

	}

	protected Map<String, VisualFeatureGroup> getVisualSQLFeatures(File imagedir, Set<String> usefeaturesidx,
			FeatureExtractor featureExtractor) throws IOException {

		Map<String, VisualFeatureGroup> ret;
		StringBuffer sb = new StringBuffer();

		File directory = getImagePathIn(new File(imagedir, getImageType())).getParentFile();
		for (File f : directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				return arg0.getName().endsWith("ftr");
			}
		})) {

			FileReader fr;
			try {
				// String
				// featurename=f.getName().substring(0,f.getName().indexOf("\\."));

				fr = new FileReader(f);
				BufferedReader br = new BufferedReader(fr);
				String line = null;

				while ((line = br.readLine()) != null) {
					if (sb.length() > 0)
						sb.append('\n');
					sb.append(line);
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (sb.toString().trim().length() > 0) {
			ret = Features2SQLString.fromSQLFile(sb.toString());
		} else {
			ret = new HashMap<String, VisualFeatureGroup>();
		}

		if (ret.size() > 0) {
			hasvisualfeatures = true;
		}
		HashSet<String> missingfeatures = new HashSet<String>();
		for (String k : usefeaturesidx) {
			if (!ret.containsKey(k)) {
				missingfeatures.add(k);
			}
		}
		if (missingfeatures.size() == 0) {
			visualFeatures = ret;
			return ret;
		}

		Map<String, VisualFeatureGroup> computedmissingfeatures = computeVisualFeatures(imagedir, missingfeatures,
				featureExtractor);

		if (computedmissingfeatures != null && computedmissingfeatures.size() > 0) {
			ret.putAll(computedmissingfeatures);
			// storeVisualFeatures(directory, computedmissingfeatures);
		}

		return visualFeatures = ret;

	}

	private String getImageType() {
		// TODO Auto-generated method stub
		return imagetype;
	}

	public boolean hasVisualData() {
		return hasvisualfeatures;
	}

	protected Map<String, VisualFeatureGroup> computeVisualFeatures(File imagedir, Set<String> usefeaturesidx,
			FeatureExtractor featureExtractor) throws IOException {

		HashMap<String, VisualFeatureGroup> tmpfeatures = new HashMap<String, VisualFeatureGroup>();

		BufferedImage image = readImage(imagedir);

		if (image != null && featureExtractor != null) {
			Hashtable<String, String> featuredescriptions;
			try {
				try {
					featuredescriptions = featureExtractor.extractFrom(image, usefeaturesidx);
				} catch (Exception e) {
					e.printStackTrace();
					return tmpfeatures;
				} catch (Throwable t) {
					t.printStackTrace();
					return tmpfeatures;
				}

				storeFeatureDescriptions(imagedir, featuredescriptions);
				Map<String, VisualFeatureGroup> visualfeatures = converter.convertToFeatures(id, featuredescriptions);

				tmpfeatures.putAll(visualfeatures);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return tmpfeatures;
	}

	public boolean hasTextualData() {
		return (pictureTitle != null && pictureTitle.trim().length() > 0)
				|| (pictureTags != null && pictureTags.trim().length() > 0);
	}

	String pictureTitle;

	public String getPictureTitle() {
		return pictureTitle;
	}

	public String getPictureTags() {
		return pictureTags;
	}

	public String getPictureDescription() {
		return pictureDescription;
	}

	public String getImageURL() {
		return imageURL;
	}

	String pictureTags;
	String pictureDescription;
	String imageURL;
	// BufferedImage image;
	private String id;

	private String filename;
	private PictureError errorinfo;
	private String error;
	private Map<String, VisualFeatureGroup> visualFeatures;

	public void setTextualMetadata(String pictureTitle, String pictureTags, String pictureDescription) {
		this.pictureTitle = pictureTitle;
		this.pictureTags = pictureTags;
		this.pictureDescription = pictureDescription;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Picture(String imagetype) {
		this.imagetype = imagetype;

	}

	/*
	 * public Picture(String id) {
	 * 
	 * this.id = id; }
	 */

	/*
	 * public void readLowLevelFeatures(InputStream imagecontents, Set<String>
	 * usefeaturesidx, FeatureExtractor featureExtractor) { boolean readbytes =
	 * false; BufferedImage image = null; if (imageURL == null && imagecontents
	 * != null) { try { image = resizeImage(ImageIO.read(imagecontents), 500,
	 * 500); readbytes = true; // return; } catch (IOException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); error = e.getMessage(); }
	 * } if (imageURL != null) { if (readbytes == true) { if (this.errorinfo ==
	 * null) { this.errorinfo = new PictureError(); }
	 * errorinfo.addWarning(wcode.URL_NOT_TAKEN); } else { try { image =
	 * resizeImage(ImageIO.read(new URL(imageURL)), 500, 500); // return; }
	 * catch (Exception e) {
	 * 
	 * try { if (imageURL.contains("flickr.com/photo")) { image =
	 * parseFlickr(imageURL); } } catch (Exception e1) { e.printStackTrace();
	 * e1.printStackTrace(); } // TODO: check if it is a html page with an image
	 * 
	 * } }
	 * 
	 * }
	 * 
	 * if (image != null) { computeLowLevelFeatures(image, usefeaturesidx,
	 * featureExtractor); } }
	 */

	public static Map<String, VisualFeature> featuresToList(Map<String, VisualFeatureGroup> features) {

		HashMap<String, VisualFeature> ret = new HashMap<String, VisualFeature>();
		if (features != null)
			for (String key : features.keySet()) {

				VisualFeatureGroup group = features.get(key);

				for (String fname : group.getFeatures().keySet()) {

					ret.put(fname, group.getFeatures().get(fname));
				}
			}
		return ret;

	}

	public static BufferedImage resizeImage(BufferedImage image, int maxWidth, int maxHeight) {
		Dimension largestDimension = new Dimension(maxWidth, maxHeight);

		// Original size
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);

		if (imageWidth <= maxWidth && imageHeight <= maxHeight) {
			return image;
		}

		float aspectRatio = (float) imageWidth / imageHeight;

		if (imageWidth > maxWidth || imageHeight > maxHeight) {
			if ((float) largestDimension.width / largestDimension.height > aspectRatio) {
				largestDimension.width = (int) Math.ceil(largestDimension.height * aspectRatio);
			} else {
				largestDimension.height = (int) Math.ceil(largestDimension.width / aspectRatio);
			}

			imageWidth = largestDimension.width;
			imageHeight = largestDimension.height;

		}
		BufferedImage newImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = newImage.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setBackground(Color.white);
			g.clearRect(0, 0, imageWidth, imageHeight);
			g.drawImage(image, 0, 0, imageWidth, imageHeight, null);
		} finally {
			g.dispose();
		}
		return newImage;

	}

	public PictureError getError() {
		return errorinfo;
	}

	public void addError(de.l3s.image.PictureError.ecode c) {
		if (errorinfo == null) {
			errorinfo = new PictureError();
		}
		errorinfo.addError(c);

	}

	public void addWarning(de.l3s.image.PictureError.wcode c) {
		if (errorinfo == null) {
			errorinfo = new PictureError();
		}
		errorinfo.addWarning(c);

	}

	public void storePictureFromBytes(File tmppicfolder, byte[] data) {

		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(data, 0, data.length));
			if (img.getWidth() > 500 || img.getHeight() > 500)
				img = resizeImage(img, 500, 500);
			ImageIO.write(img, "jpg", tmppicfolder);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void storePictureFromBytes(File path, BufferedImage img) throws IOException {

		if (path.exists()) {
			return;
		}
		if (img.getWidth() > 500 || img.getHeight() > 500)
			img = resizeImage(img, 500, 500);
		ImageIO.write(img, "jpg", path);

	}

	public void updateTo(Picture p) {
		if (p.id != null && p.id.trim().length() > 0) {
			this.id = p.id;
		}

		this.pictureDescription = p.pictureDescription;
		this.pictureTags = p.pictureTags;
		this.pictureTitle = p.pictureTitle;
	}

	public Map<String, VisualFeature> featuresToList(HashSet<String> usefeaturesidx, FeatureExtractor featureExtractor)
			throws IOException {

		return featuresToList(getVisualFeatures(usefeaturesidx, featureExtractor));
	}

	public void computeVisualFeatures(HashSet<String> usefeaturesidx, FeatureExtractor featureExtractor)
			throws IOException {

		visualFeatures = computeVisualFeatures(imgpath, usefeaturesidx, featureExtractor);

	}

	public Map<String, VisualFeature> featuresToList() {
		return featuresToList(visualFeatures);
	}

	public Map<String, VisualFeatureGroup> getVisualFeatures() {
		return visualFeatures;

	}

	public void gatherVisualFeatures(HashSet<String> usefeaturesidx, ImageFeatureExtractor imageFeatureExtractor) {

	}

	public abstract boolean exists(File imagedir);

	public void destruct() {
		visualFeatures = null;
		converter = null;

	}
}
