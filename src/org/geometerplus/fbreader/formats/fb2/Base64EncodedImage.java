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

package org.geometerplus.fbreader.formats.fb2;

import org.geometerplus.zlibrary.core.util.ZLTextBuffer;
import org.geometerplus.zlibrary.core.image.ZLSingleImage;

final class Base64EncodedImage extends ZLSingleImage {
	private ZLTextBuffer myEncodedData = new ZLTextBuffer();
	private byte[] myData;
	
	public Base64EncodedImage(String contentType) {
		// TODO: use contentType
		super(contentType);
	}

	private static byte decodeByte(char encodedChar) {
		switch (encodedChar) {
			case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
			case 'G': case 'H': case 'I': case 'J': case 'K': case 'L':
			case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R':
			case 'S': case 'T': case 'U': case 'V': case 'W': case 'X':
			case 'Y': case 'Z':
				return (byte)(encodedChar - 'A');
			case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
			case 'g': case 'h': case 'i': case 'j': case 'k': case 'l':
			case 'm': case 'n': case 'o': case 'p': case 'q': case 'r':
			case 's': case 't': case 'u': case 'v': case 'w': case 'x':
			case 'y': case 'z':
				return (byte)(encodedChar - 'a' + 26);
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				return (byte)(encodedChar - '0' + 52);
			case '+':
				return 62;
			case '/':
				return 63;
			case '=':
				return 64;
		}
		return -1;
	}

	private void decode() {
		if ((myEncodedData == null) || (myData != null)) {
			return;
		}

		final char[] encodedData = myEncodedData.getData();
		final int dataLength = myEncodedData.getLength();
		
		final int newLength = dataLength * 3 / 4;
		final byte[] data = new byte[newLength];
		for (int pos = 0, dataPos = 0; pos < dataLength; ) {
			byte n0 = -1, n1 = -1, n2 = -1, n3 = -1;
			while ((pos < dataLength) && (n0 == -1)) {
				n0 = decodeByte(encodedData[pos++]);
			}
			while ((pos < dataLength) && (n1 == -1)) {
				n1 = decodeByte(encodedData[pos++]);
			}
			while ((pos < dataLength) && (n2 == -1)) {
				n2 = decodeByte(encodedData[pos++]);
			}
			while ((pos < dataLength) && (n3 == -1)) {
				n3 = decodeByte(encodedData[pos++]);
			}
			data[dataPos++] = (byte) (n0 << 2 | n1 >> 4);
			data[dataPos++] = (byte) (((n1 & 0xf) << 4) | ((n2 >> 2) & 0xf));
			data[dataPos++] = (byte) (n2 << 6 | n3);
		}
		myData = data;
		myEncodedData = null;
	}
	
	public byte[] byteData() {
		decode();
		return myData;
	}
	
	void addData(char[] data, int offset, int length) {
		myEncodedData.append(data, offset, length);
	}

	void trimToSize() {
		myEncodedData.trimToSize();
	}
}
