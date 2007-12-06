package org.zlibrary.text.view.impl;

import org.zlibrary.core.image.ZLImage;

/*package*/ class ZLTextImageElement extends ZLTextElement {
	private ZLImage myImage;
	private String myId;

	/*package*/ ZLTextImageElement(String id, ZLImage image) {
		myId = id;
		myImage = image;
	}

	public String getId() {
		return myId;
	}

	public ZLImage getImage() {
		return myImage;
	}	
}
