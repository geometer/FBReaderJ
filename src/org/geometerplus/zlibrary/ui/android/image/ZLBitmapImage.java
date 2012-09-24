package org.geometerplus.zlibrary.ui.android.image;

import org.geometerplus.zlibrary.core.image.ZLImage;

import android.graphics.Bitmap;

public class ZLBitmapImage implements ZLImage {

	private Bitmap myBitmap = null;
	
	public ZLBitmapImage(Bitmap b) {
		myBitmap = b;
	}
	
	public Bitmap getBitmap() {
		return myBitmap;
	}
	
	@Override
	public String getURI() {
		return null;
	}

}
