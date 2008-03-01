package org.zlibrary.text.view.impl;

import org.zlibrary.core.image.ZLImageData;

public final class ZLTextImageElement extends ZLTextElement {
	private ZLImageData myImageData;
	private String myId;
    
	@Deprecated
    ZLTextImageElement(ZLImageData imageData) {
		myImageData = imageData;
		myId = "";
	}

	public ZLImageData getImageData() {
		return myImageData;
	}
	
	public String getId() {
		return myId;
	}
	//new version	
	ZLTextImageElement(String id, ZLImageData image) {
		 myId = id;
		 myImageData = image;
	}

}

