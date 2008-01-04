package org.zlibrary.ui.android.image;

import android.graphics.BitmapFactory;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.core.image.ZLImageManager;

public final class ZLAndroidImageManager extends ZLImageManager {
	public ZLAndroidImageData getImageData(ZLImage image) {
		byte[] array = image.byteData();
		return new ZLAndroidImageData(BitmapFactory.decodeByteArray(array, 0, array.length));
	}
}
