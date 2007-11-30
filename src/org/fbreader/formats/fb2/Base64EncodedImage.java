package org.fbreader.formats.fb2;

import org.apache.commons.codec.binary.Base64;
import org.zlibrary.core.image.ZLImage;

public class Base64EncodedImage implements ZLImage {
	private String myEncodedData;
	private byte [] myData;
	
	public Base64EncodedImage(String contentType) {
		// TODO Auto-generated constructor stub
	}

	private void decode() {
		if ((myEncodedData == null) || (myData != null)) {
			return;
		}
		myData = Base64.decodeBase64(myEncodedData.getBytes());
		myEncodedData = null;
	}
	
	public byte [] byteData() {
		decode();
		return myData;
	}
	
	public void addData(StringBuffer data) {
		myEncodedData = data.toString();
	}
	
}
