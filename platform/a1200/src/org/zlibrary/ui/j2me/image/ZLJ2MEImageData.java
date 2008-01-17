package org.zlibrary.ui.j2me.image;

import javax.microedition.lcdui.Image;

import org.zlibrary.core.image.ZLImageData;

public final class ZLJ2MEImageData implements ZLImageData {
	private Image myImage;

	ZLJ2MEImageData(Image image) {
		myImage = image;
	}

	public Image getImage() {
		return myImage;
	}
}
