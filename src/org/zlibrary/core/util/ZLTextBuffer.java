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
			data = ZLArrayUtils.createCopy(data, oldLen, capacity);
			myData = data;
		}
		System.arraycopy(buffer, offset, data, oldLen, count);
		myDataLength = newLen;
	}

	public void trimToSize() {
		final int len = myDataLength;
		final char[] data = myData;
		if (len < data.length) {
			myData = ZLArrayUtils.createCopy(data, len, len);
		}
	}

	public void clear() {
		myDataLength = 0;
	}
}
