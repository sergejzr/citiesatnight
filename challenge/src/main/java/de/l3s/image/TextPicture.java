package de.l3s.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class TextPicture extends Picture {

	public TextPicture() {
		super("textpicture");
	}

	@Override
	public BufferedImage downloadImage() throws MalformedURLException,
			IOException {
		return null;
	}

	@Override
	protected File getImagePathIn(File directory) {
		return null;
	}

	@Override
	public String getStorageId() {
		// TODO Auto-generated method stub
		return getId();
	}

	@Override
	public boolean exists(File imagedir) {
		// TODO Auto-generated method stub
		return true;
	}

}
