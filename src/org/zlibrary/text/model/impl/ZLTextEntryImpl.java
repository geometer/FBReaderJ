package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.entry.ZLTextEntry;

class ZLTextEntryImpl implements ZLTextEntry {
	private String myData;

	ZLTextEntryImpl(String data) {
		myData = data;
	}

	public int getDataLength() {
		return myData.length();
	}

	public String getData() {
		return myData;
	}
}
