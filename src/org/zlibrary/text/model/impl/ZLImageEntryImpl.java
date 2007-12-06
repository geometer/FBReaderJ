package org.zlibrary.text.model.impl;

import java.util.Map;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.text.model.entry.ZLImageEntry;

class ZLImageEntryImpl implements ZLImageEntry {
	private String myId;
	private Map<String, ZLImage> myMap;
	private short myVOffset;

	ZLImageEntryImpl(String id, Map<String, ZLImage> imageMap, short vOffset) {
		this.myId = id;
		this.myMap = imageMap;
		this.myVOffset = vOffset;
	}
	
	public String getId() {
		return myId;
	}
	
	public short getVOffset() {
		return myVOffset;
	}
	
	public ZLImage getImage() {
		return myMap.get(myId);
	}
}
