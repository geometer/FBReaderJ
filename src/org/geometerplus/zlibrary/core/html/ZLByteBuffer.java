/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.core.html;

import java.io.UnsupportedEncodingException;

import org.geometerplus.zlibrary.core.util.ZLArrayUtils;

public final class ZLByteBuffer {
	private byte[] myData;
	private int myLength;

	ZLByteBuffer(int len) {
		myData = new byte[len];
	}

	ZLByteBuffer() {
		this(20);
	}

	ZLByteBuffer(ZLByteBuffer container) {
		final int len = container.myLength;
		myData = ZLArrayUtils.createCopy(container.myData, len, len);
		myLength = len;
	}

	public void append(byte[] buffer, int offset, int count) {
		final int len = myLength;
		byte[] data = myData;
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
		final ZLByteBuffer container = (ZLByteBuffer)o;
		final int len = myLength;
		if (len != container.myLength) {
			return false;
		}
		final byte[] data0 = myData;
		final byte[] data1 = container.myData;
		for (int i = len; --i >= 0; ) {
			if (data0[i] != data1[i]) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		final int len = myLength;
		final byte[] data = myData;
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

	public byte[] data() {
		return myData;
	}

	public int length() {
		return myLength;
	}

	public boolean equalsToLCString(String lcPattern) {
		return (myLength == lcPattern.length()) &&
				lcPattern.equals(new String(myData, 0, myLength).toLowerCase());
	} 

	public String toString(String encoding) {
		try {
			return new String(myData, 0, myLength, encoding);
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
}
