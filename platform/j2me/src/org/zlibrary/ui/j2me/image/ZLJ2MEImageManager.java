package org.geometerplus.zlibrary.ui.j2me.image;

import javax.microedition.lcdui.Image;

import org.geometerplus.zlibrary.core.image.*;

public final class ZLJ2MEImageManager extends ZLImageManager {
	public ZLImageData getImageData(ZLImage image) {
		byte[] array = image.byteData();
		return new ZLJ2MEImageData(Image.createImage(array, 0, array.length));
	}
}
