package org.zlibrary.model.impl;

import org.zlibrary.model.entry.ZLTextFixedHSpaceEntry;

class ZLTextFixedHSpaceEntryImpl implements ZLTextFixedHSpaceEntry {
	private byte myLength;

	ZLTextFixedHSpaceEntryImpl(byte length) {
		this.myLength = length;
	}
	
	public byte length() {
		return this.myLength;
	}
}
