package org.zlibrary.ui.j2me.image;

//import android.graphics.BitmapFactory;

import org.zlibrary.core.image.*;

public final class ZLJ2MEImageManager extends ZLImageManager {
	public ZLImageData getImageData(ZLImage image) {
		//byte[] array = image.byteData();
		//return new ZLAndroidImageData(BitmapFactory.decodeByteArray(array, 0, array.length));
		return new ZLJ2MEImageData();
	}
}
