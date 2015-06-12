package org.geometerplus.zlibrary.core.util;

import android.graphics.Bitmap;

public abstract class BitmapUtil {
	public static Bitmap createBitmap(int width, int height, Bitmap.Config c) {
		try {
			return Bitmap.createBitmap(width, height, c);
		} catch (OutOfMemoryError e) {
			System.gc();
			System.gc();
			return Bitmap.createBitmap(width, height, c);
		}
	}

	public static Bitmap createBitmap(int width, int height) {
		return createBitmap(width, height, Bitmap.Config.RGB_565);
	}
}
