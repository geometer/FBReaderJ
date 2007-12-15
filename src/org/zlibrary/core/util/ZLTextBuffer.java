package org.zlibrary.core.util;

public final class ZLTextBuffer {
	private char[] myData;
	private int myDataLength;

	public ZLTextBuffer(int initialCapacity) {
		myData = new char[initialCapacity];
	}

	public ZLTextBuffer() {
		this(8192);
	}

	public char[] getData() {
		return myData;
	}

	public int getLength() {
		return myDataLength;
	}

	public boolean isEmpty() {
		return myDataLength == 0;
	}

	public void append(char[] data) {
		append(data, 0, data.length);
	}

	public void append(char[] data, int offset, int length) {
		final int newDataLength = myDataLength + length;
		if (newDataLength > myData.length) {
			int newArrayLength = myData.length * 2;
			if (newArrayLength < 64) {
				newArrayLength = 64;
			}
			while (newDataLength > newArrayLength) {
				newArrayLength = newArrayLength * 2;
			}
			char[] newData = new char[newArrayLength];
			if (myDataLength > 0) {
				System.arraycopy(myData, 0, newData, 0, myDataLength);
			}
			myData = newData;
		}
		/*
		int srcpos = offset;
		int dstpos = myDataLength;
		char[] dst = myData;
		for (int i = 0; i < length; ++i) {
			dst[dstpos++] = data[srcpos++];	
		}
		*/
		System.arraycopy(data, offset, myData, myDataLength, length);
		myDataLength = newDataLength;
	}

	public void trimToSize() {
		if (myDataLength < myData.length) {
			char[] newData = new char[myDataLength];
			if (myDataLength > 0) {
				System.arraycopy(myData, 0, newData, 0, myDataLength);
			}
			myData = newData;
		}
	}

	public void clear() {
		myDataLength = 0;
	}
}
