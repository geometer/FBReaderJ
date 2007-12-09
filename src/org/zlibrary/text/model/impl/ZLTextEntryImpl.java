package org.zlibrary.text.model.impl;

import java.util.ArrayList;

import org.zlibrary.text.model.entry.ZLTextEntry;

class ZLTextEntryImpl implements ZLTextEntry {
	private char[] myData;

	ZLTextEntryImpl(char[] data) {
		myData = data;
	}

	ZLTextEntryImpl(ArrayList<char[]> data) {
		int length = 0;
		for (char[] part : data) {
			length += part.length;
		}
		myData = new char[length];
		int pos = 0;
		for (char[] part : data) {
			System.arraycopy(part, 0, myData, pos, part.length);
			pos += part.length;
		}
	}

	public int getDataLength() {
		return myData.length;
	}

	public char[] getData() {
		return myData;
	}
}
