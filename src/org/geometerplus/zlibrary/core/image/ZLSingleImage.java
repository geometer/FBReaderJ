package org.geometerplus.zlibrary.core.image;

public abstract class ZLSingleImage implements ZLImage {
	private final String myMimeType;
	
	public ZLSingleImage(final String mimeType) {
		myMimeType = mimeType;
	}

	public abstract byte [] byteData();
	
	public final String mimeType() {
		return myMimeType;
	}
}
