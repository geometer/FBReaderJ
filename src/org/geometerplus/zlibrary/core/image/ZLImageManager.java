package org.geometerplus.zlibrary.core.image;

public abstract class ZLImageManager {
	private static ZLImageManager ourInstance;

	public static ZLImageManager getInstance() {
		return ourInstance;
	}

	protected ZLImageManager() {
		ourInstance = this;
	}

	public abstract ZLImageData getImageData(ZLImage image);
}
