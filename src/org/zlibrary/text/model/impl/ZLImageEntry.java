package org.zlibrary.text.model.impl;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.core.image.ZLImageMap;
import org.zlibrary.text.model.ZLTextParagraph;

public final class ZLImageEntry {
	private final ZLImageMap myImageMap;
	private final String myId;
	public final short VOffset;

	ZLImageEntry(ZLImageMap imageMap, String id, short vOffset) {
		myImageMap = imageMap;
		myId = id;
		VOffset = vOffset;
	}
	
	public String getId() {
		return myId;
	}

	public ZLImage getImage() {
		return myImageMap.getImage(myId);
	}
}
