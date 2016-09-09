package de.l3s.image;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import sz.de.l3s.features.util.FileDistributer;



public class FlickrPicture extends URLPicture {

	private String flickrpageURL;
	private String flickrid;

	public FlickrPicture(String flickrpageURL) {
		super(null,"flickr");
		this.flickrpageURL=flickrpageURL;
	}
@Override
public String getStorageId() {
	// TODO Auto-generated method stub
	return flickrid;
}
	@Override
	public BufferedImage downloadImage() throws MalformedURLException,
			IOException {
		parseFlickr(flickrpageURL);
		return super.downloadImage();
	}
	@Override
	protected File getImagePathIn(File directory) {
		FileDistributer fe=new FileDistributer(flickrid, directory,true);
		return fe.extendFile(".jpg");
	}
@Override
public boolean exists(File imagedir) {
	if(flickrid==null);
	parseFlickr(flickrpageURL);
	return super.exists(imagedir);
}
	private void parseFlickr(String rurl) {

		if(rurl.contains("static.flickr.com")||rurl.contains("staticflickr.com"))
				{
			imageURL=rurl;
			int idx=imageURL.lastIndexOf("/");
			 idx=imageURL.indexOf("_",idx);
			flickrid=imageURL.substring(imageURL.lastIndexOf("/")+1,idx);
			return;
				}
		if(flickrid!=null) return;
		String content = loadContent(rurl);
		
		String hurl = rurl.replaceAll("/", " ").trim();
		
		String[] parts = hurl.split(" ");
		flickrid=parts[parts.length-1];
	
		Vector<String> tagsv = getStrings(content, "data-ywa-name=\"Tag(s)\">",
				"<");
		pictureTags = "";
		for (String tag : tagsv) {
			if (pictureTags.length() > 0)
				pictureTags += ",";
			pictureTags += tag;
		}
		// tags=getString(content,"<meta name=\"keywords\" content=\"",">");
		pictureTitle = getString(content, "<meta name=\"title\" content=\"",
				">");
		pictureDescription = getString(content,
				"<meta name=\"description\" content=\"", ">");

		String FlickrImageUrl = getString(content,
				"<link rel=\"image_src\" href=\"", "\"");

			
			imageURL=FlickrImageUrl.endsWith("_m.jpg")
									|| FlickrImageUrl.endsWith("_s.jpg") ? FlickrImageUrl
									.substring(0, FlickrImageUrl.length() - 6)
									+ "_m.jpg" : FlickrImageUrl;
		
	
	}
	
	private Vector<String> getStrings(String content, String start, String end) {
		Vector<String> ret = new Vector<String>();
		int curidx = 0;
		boolean ready = false;
		int tcntmax = 1000;
		while (!ready && tcntmax > 0) {
			int idx1 = content.indexOf(start, curidx);
			if (idx1 < 0) {
				ready = true;
				break;
			}
			idx1 += start.length();
			int idx2 = content.indexOf(end, idx1);
			if (idx2 < 0) {
				ready = true;
				break;
			}

			ret.add(content.substring(idx1, idx2));
			curidx = idx2 + end.length();
			tcntmax--;
		}

		return ret;
	}

	private String getString(String content, String start, String end) {

		int idx1 = content.indexOf(start) + start.length();
		int idx2 = content.indexOf(end, idx1);
		return content.substring(idx1, idx2);
	}
	
	private String loadContent(String getUrl) {
		StringBuffer sb = new StringBuffer();
		try {

			URL url = new URL(getUrl);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String line = null;
			int i = 0;

			while ((line = in.readLine()) != null && i++ < 100000) {
				sb.append(line + "\n");
			}

		} catch (Exception e) {
			sb.append(e.toString());
		}
		return sb.toString();
	}
	
	/*
	public void readLowLevelFeatures(InputStream imagecontents, Set<String>
	usefeaturesidx, FeatureExtractor featureExtractor) {
		
		boolean readbytes =
	false; 
		BufferedImage image = null; 
		if (imageURL == null && imagecontents
	!= null) { try { image = resizeImage(ImageIO.read(imagecontents), 500,
	500); readbytes = true; // return; } 
	catch (IOException e) { 
 catch block e.printStackTrace(); error = e.getMessage(); }
	
	} if (imageURL != null) { if (readbytes == true) { if (this.errorinfo ==
	null) { this.errorinfo = new PictureError(); }
	errorinfo.addWarning(wcode.URL_NOT_TAKEN); } else { try { image =
	resizeImage(ImageIO.read(new URL(imageURL)), 500, 500); // return; }
	catch (Exception e) {

	try { if (imageURL.contains("flickr.com/photo")) { image =
	parseFlickr(imageURL); } } catch (Exception e1) { e.printStackTrace();
	e1.printStackTrace(); } // TODO: check if it is a html page with an image

	} }

	}

	if (image != null) { computeLowLevelFeatures(image, usefeaturesidx,
	featureExtractor); } }
	*/
}
