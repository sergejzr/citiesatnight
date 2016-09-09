package de.l3s.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import sz.de.l3s.features.util.FileDistributer;
import sz.de.l3s.features.util.MD5;







public class URLPicture extends Picture {

	String urlbasedid=null;
	public URLPicture(String imageURL,String type)
	{
		super(type);
	}
	public URLPicture(String imageURL) {
		super("urlbased");
		this.imageURL = imageURL;
		
		
			 
			
			try {
			
				urlbasedid = MD5.encode(imageURL.getBytes("UTF-8")).substring(0, 9);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
	}

	@Override
	public BufferedImage downloadImage() throws MalformedURLException,
			IOException {
		
		System.out.println("imageURL: "+imageURL);
		try{
		return ImageIO.read(new URL(imageURL));
		}catch (Exception e) {
			e.printStackTrace();
		}
return null;
	}

	@Override
	protected File getImagePathIn(File directory) {
		
		FileDistributer fd = new FileDistributer(urlbasedid, directory,
				true);
		return fd.extendFile(".jpg");
		
	}
	@Override
	public String getStorageId() {
		
		return urlbasedid;
	}
	@Override
	public boolean exists(File imagedir) {
		File f=getImagePathIn(new File(imagedir,imagetype));
		return (f.exists());
	}
}
