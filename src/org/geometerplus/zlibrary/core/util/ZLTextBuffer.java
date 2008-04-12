/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.util;

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
