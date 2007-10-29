package org.zlibrary.model.impl;

import org.zlibrary.model.entry.ZLTextParagraphEntry;

class ZLTextFixedHSpaceEntryImpl implements ZLTextParagraphEntry {
	private byte myLength;

	ZLTextFixedHSpaceEntryImpl(byte length) {
		this.myLength = length;
	}
	
	public byte length() {
		return this.myLength;
	}
}
