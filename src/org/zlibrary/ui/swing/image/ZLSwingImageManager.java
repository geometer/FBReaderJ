package org.zlibrary.ui.swing.image;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.core.image.ZLImageManager;

public final class ZLSwingImageManager extends ZLImageManager {
	public ZLSwingImageData getImageData(ZLImage image) {
		try {
			final BufferedImage awtImage = ImageIO.read(new ByteArrayInputStream(image.byteData()));
			return new ZLSwingImageData(awtImage);
		} catch (IOException e) {
			return null;
		}
	}
}
