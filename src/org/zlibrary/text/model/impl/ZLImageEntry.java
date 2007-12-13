package org.zlibrary.text.model.impl;

import java.util.Map;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.text.model.ZLTextParagraph;

public class ZLImageEntry implements ZLTextParagraph.Entry {
	private final String myId;
	private final Map<String,ZLImage> myImageMap;
	public final short VOffset;

	ZLImageEntry(String id, Map<String,ZLImage> imageMap, short vOffset) {
		myId = id;
		myImageMap = imageMap;
		VOffset = vOffset;
	}
	
	public ZLImage getImage() {
		return myImageMap.get(myId);
	}
}
