package org.zlibrary.model.impl;

import org.zlibrary.model.ZLTextParagraphEntry;

class ZLTextFixedHSpaceEntryImpl implements ZLTextParagraphEntry {
	private byte myLength;

	ZLTextFixedHSpaceEntryImpl(byte length) {
		this.myLength = length;
	}
	
	public byte length() {
		return this.myLength;
	}
}
