package org.zlibrary.core.xml.own;

import org.zlibrary.core.util.ZLArrayUtils;

final class ZLMutableString {
	private char[] myData;
	private int myLength;

	ZLMutableString(int len) {
		myData = new char[len];
	}

	ZLMutableString() {
		this(20);
	}

	ZLMutableString(ZLMutableString container) {
		final int len = container.myLength;
		myData = ZLArrayUtils.createCopy(container.myData, len, len);
		myLength = len;
	}

	public void append(char[] buffer, int offset, int count) {
		final int len = myLength;
		char[] data = myData;
		final int newLength = len + count;
		if (data.length < newLength) {
			data = ZLArrayUtils.createCopy(data, len, newLength);
			myData = data;
		}
		System.arraycopy(buffer, offset, data, len, count);
		myLength = newLength;
	}

	public void clear() {
		myLength = 0;
	}

	public boolean equals(Object o) {
		final ZLMutableString container = (ZLMutableString)o;
		final int len = myLength;
		if (len != container.myLength) {
			return false;
		}
		final char[] data0 = myData;
		final char[] data1 = container.myData;
		for (int i = len; --i >= 0; ) {
			if (data0[i] != data1[i]) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		final int len = myLength;
		final char[] data = myData;
		int code = len * 31;
		if (len > 1) {
			code += data[0];
			code *= 31;
			code += data[1];
			if (len > 2) {
				code *= 31;
				code += data[2];
			}
		} else if (len > 0) {
			code += data[0];
		}
		return code;
	}

	public String toString() {
		return new String(myData, 0, myLength).intern();
	}
}
