package org.zlibrary.model.impl;

import java.util.Map;

import org.zlibrary.model.ZLTextParagraphEntry;

class ImageEntryImpl implements ZLTextParagraphEntry {
	private String myId;
	private Map<String, ZLImage> myMap;
	private short myVOffset;

	ImageEntryImpl(String id, Map<String, ZLImage> imageMap, short vOffset) {
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
	
	public ZLImage image() {
		return myMap.get(myId);
	}
}
