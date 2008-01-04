package org.zlibrary.text.view.impl;

import org.zlibrary.core.image.ZLImageData;

class ZLTextImageElement extends ZLTextElement {
	private ZLImageData myImageData;

	ZLTextImageElement(ZLImageData imageData) {
		myImageData = imageData;
	}

	public ZLImageData getImageData() {
		return myImageData;
	}	
}
