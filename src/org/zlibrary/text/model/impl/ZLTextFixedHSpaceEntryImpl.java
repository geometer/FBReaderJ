package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.entry.ZLTextFixedHSpaceEntry;

class ZLTextFixedHSpaceEntryImpl implements ZLTextFixedHSpaceEntry {
	private byte myLength;

	ZLTextFixedHSpaceEntryImpl(byte length) {
		this.myLength = length;
	}
	
	public byte length() {
		return this.myLength;
	}
}
