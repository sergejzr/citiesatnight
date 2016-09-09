package de.l3s.image;


import java.util.Vector;



public class PictureError {
	/**
	 * Error codes
	 */
	public static enum ecode {
		NOT_ENOUGH_DATA, NO_USABLE_FEATURES, CLASSIFICATION_ERROR, DOWNLOAD_FAILED, FEATUREEXTRACTION_FAILED,
	}

	/**
	 * Warning codes
	 */
	public static enum wcode {
		URL_NOT_TAKEN,
	}


	private Vector<PictureError.ecode> _errorcodes;

	private Vector<PictureError.wcode> _warningcodes;

	public PictureError() {
	}

	public Vector<PictureError.ecode> getErrorCodes() {
		return _errorcodes;
	}
	
	public Vector<PictureError.wcode> getWarningCodes() {
		return _warningcodes;
	}

	public void addError(ecode c) {
		if (_errorcodes == null) {
			_errorcodes = new Vector<PictureError.ecode>();
		}
		_errorcodes.add(c);
	}

	public void addWarning(wcode c) {
		if (_warningcodes == null) {
			_warningcodes = new Vector<PictureError.wcode>();
		}
		_warningcodes.add(c);
	}

	public static String warningToString(wcode c) {
		switch (c) {
		case URL_NOT_TAKEN: {
			return "URL ignored, because supplied together with byte data.";
		}
		default:
			return "UNKNOWN warning message " + c;
		}
	}

	public static String errorToString(ecode c) {
		switch (c) {
		case NOT_ENOUGH_DATA: {
			return "not enough data for classification";
		}
		case NO_USABLE_FEATURES:
		{
			return "no usable features could be extracted from this image.";
		}
		case CLASSIFICATION_ERROR:{
			return "CLASSIFICATION_ERROR";
		}
		case DOWNLOAD_FAILED:{
			return "DOWNLOAD_FAILED";
		}
		case FEATUREEXTRACTION_FAILED:{
			return "FEATUREEXTRACTION_FAILED";
		}
		default:
			return "UNKNOWN error message " + c;
		}
	}
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		for(ecode _ecode : _errorcodes)
		{
			sb.append(errorToString(_ecode)+"\n");
		};
		return sb.toString();
	}
}

