package org.zlibrary.text.view.impl;

import org.zlibrary.core.image.ZLImage;

class ZLTextImageElement extends ZLTextElement {
	private ZLImage myImage;

	ZLTextImageElement(ZLImage image) {
		myImage = image;
	}

	public ZLImage getImage() {
		return myImage;
	}	
}
