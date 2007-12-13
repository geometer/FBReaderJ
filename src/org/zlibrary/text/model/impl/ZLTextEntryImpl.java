package org.zlibrary.text.model.impl;

import java.util.ArrayList;

import org.zlibrary.text.model.entry.ZLTextEntry;

final class ZLTextEntryImpl implements ZLTextEntry {
	private final char[] myData;
	private final int myDataOffset;
	private final int myDataLength;

	ZLTextEntryImpl(char[] data, int offset, int length) {
		myData = data;
		myDataOffset = offset;
		myDataLength = length;
	}

	public char[] getData() {
		return myData;
	}

	public int getDataLength() {
		return myDataLength;
	}

	public int getDataOffset() {
		return myDataOffset;
	}
}
