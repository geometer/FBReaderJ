package org.zlibrary.ui.swing.image;

import java.awt.image.BufferedImage;

import org.zlibrary.core.image.ZLImageData;

public final class ZLSwingImageData implements ZLImageData {
	private final BufferedImage myImage;

	ZLSwingImageData(BufferedImage image) {
		myImage = image;
	}

	public BufferedImage getAwtImage() {
		return myImage;
	}
}
