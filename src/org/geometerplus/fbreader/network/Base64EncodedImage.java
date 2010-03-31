/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network;

import java.io.*;

import org.geometerplus.zlibrary.core.image.ZLSingleImage;

import org.geometerplus.fbreader.Constants;

final class Base64EncodedImage extends ZLSingleImage {

	private String myData;
	private String myFileName;

	// mimeType string MUST be interned
	public Base64EncodedImage(String mimeType, String data) {
		super(mimeType);
		myData = data;
		new File(makeImagesDir()).mkdirs();
	}

	public static String makeImagesDir() {
		return NetworkImage.makeImagesDir() + File.separator + "base64";
	}

	private static byte decodeByte(char encodedByte) {
		switch (encodedByte) {
			case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
			case 'G': case 'H': case 'I': case 'J': case 'K': case 'L':
			case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R':
			case 'S': case 'T': case 'U': case 'V': case 'W': case 'X':
			case 'Y': case 'Z':
				return (byte)(encodedByte - 'A');
			case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
			case 'g': case 'h': case 'i': case 'j': case 'k': case 'l':
			case 'm': case 'n': case 'o': case 'p': case 'q': case 'r':
			case 's': case 't': case 'u': case 'v': case 'w': case 'x':
			case 'y': case 'z':
				return (byte)(encodedByte - 'a' + 26);
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				return (byte)(encodedByte - '0' + 52);
			case '+':
				return 62;
			case '/':
				return 63;
			case '=':
				return 64;
		}
		return -1;
	}

	public byte[] byteData() {
		try {
			decode();
			final File file = new File(myFileName);
			final byte[] data = new byte[(int)file.length()];
			final FileInputStream stream = new FileInputStream(file);
			stream.read(data);
			stream.close();
			return data;
		} catch (IOException e) {
			return null;
		}
	}

	private void decode() throws IOException {
		if (myFileName != null) {
			return;
		}
		myFileName = makeImagesDir() + File.separator + myData.hashCode();
		final String type = mimeType();
		if (type == NetworkImage.MIME_PNG) {
			myFileName += ".png";
		} else if (type == NetworkImage.MIME_JPEG) {
			myFileName += ".jpg";
		}
		FileOutputStream outputStream = null;
		try {
			File file = new File(myFileName);
			if (file.exists()) {
				// TODO: check file validity
				return;
			}
			outputStream = new FileOutputStream(file);
			final int dataLength = myData.length();
			final byte[] data = new byte[myData.length() * 3 / 4 + 4];
			int dataPos = 0;
			for (int pos = 0; pos < dataLength; ) {
				byte n0 = -1, n1 = -1, n2 = -1, n3 = -1;
				while ((pos < dataLength) && (n0 == -1)) {
					n0 = decodeByte(myData.charAt(pos++));
				}
				while ((pos < dataLength) && (n1 == -1)) {
					n1 = decodeByte(myData.charAt(pos++));
				}
				while ((pos < dataLength) && (n2 == -1)) {
					n2 = decodeByte(myData.charAt(pos++));
				}
				while ((pos < dataLength) && (n3 == -1)) {
					n3 = decodeByte(myData.charAt(pos++));
				}
				data[dataPos++] = (byte)(n0 << 2 | n1 >> 4);
				data[dataPos++] = (byte)(((n1 & 0xf) << 4) | ((n2 >> 2) & 0xf));
				data[dataPos++] = (byte)(n2 << 6 | n3);
			}
			outputStream.write(data, 0, dataPos);
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
			myData = null;
		}
	}
}
