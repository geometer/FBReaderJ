package org.zlibrary.text.model.impl;

import java.util.ArrayList;

import org.zlibrary.text.model.entry.ZLTextEntry;

final class ZLTextEntryImpl implements ZLTextEntry {
	private char[] myData;
	private ArrayList<char[]> myDataArray;
	private int myLength;

	ZLTextEntryImpl(char[] data) {
		myData = data;
		myLength = data.length;
	}

	ZLTextEntryImpl(ArrayList<char[]> data) {
		myLength = 0;
		for (char[] part : data) {
			myLength += part.length;
		}
		myDataArray = new ArrayList<char[]>(data);
	}

	public int getDataLength() {
		return myLength;
	}

	public char[] getData() {
		if (myDataArray != null) {
			convert();
		}
		return myData;
	}

	private void convert() {
		myData = new char[myLength];
		int pos = 0;
		for (char[] part : myDataArray) {
			System.arraycopy(part, 0, myData, pos, part.length);
			pos += part.length;
		}
		myDataArray = null;
	}
}
