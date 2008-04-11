package org.geometerplus.zlibrary.text.model.impl;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageMap;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;

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
