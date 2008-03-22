package org.zlibrary.text.view.impl;

import org.zlibrary.core.image.ZLImageData;

public final class ZLTextImageElement extends ZLTextElement {
	public final String Id;
	public final ZLImageData ImageData;
    
	ZLTextImageElement(String id, ZLImageData imageData) {
		Id = id;
		ImageData = imageData;
	}
}

