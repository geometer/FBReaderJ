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

	public void append(char[] buffer, int offset, int count) {
		char[] data = myData;
		final int oldLen = myDataLength;
		final int newLen = oldLen + count;
		if (newLen > data.length) {
			int capacity = data.length << 1;
			while (newLen > capacity) {
				capacity <<= 1;
			}
			char[] data1 = new char[capacity];
			if (oldLen > 0) {
				System.arraycopy(data, 0, data1, 0, oldLen);
			}
			data = data1;
			myData = data;
		}
		System.arraycopy(buffer, offset, data, oldLen, count);
		myDataLength = newLen;
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
