/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.core.image;

import java.io.*;

public abstract class ZLBase64EncodedImage implements ZLStreamImage {
	private boolean myIsDecoded;

	protected ZLBase64EncodedImage() {
	}

	protected static byte decodeByte(byte encodedByte) {
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

	public String getURI() {
		try {
			decode();
			final File file = new File(decodedFileName());
			return ZLFileImage.SCHEME + "://" + decodedFileName() + "\000\0000\000" + (int)file.length();
		} catch (Exception e) {
			return null;
		}
	}

	protected abstract String encodedFileName();
	protected abstract String decodedFileName();

	protected boolean isCacheValid(File file) {
		return false;
	}

	private void decode() throws IOException {
		if (myIsDecoded) {
			return;
		}
		myIsDecoded = true;

		final File outputFile = new File(decodedFileName());
		if (isCacheValid(outputFile)) {
			return;
		}

		FileOutputStream outputStream = new FileOutputStream(outputFile);
		try {
			int dataLength;
			byte[] encodedData;

			final File file = new File(encodedFileName());
			final FileInputStream inputStream = new FileInputStream(file);
			try {
				dataLength = (int)file.length();
				encodedData = new byte[dataLength];
				inputStream.read(encodedData);
			} finally {
				inputStream.close();
			}
			file.delete();

			final byte[] data = new byte[dataLength * 3 / 4 + 4];
			int dataPos = 0;
			for (int pos = 0; pos < dataLength; ) {
				byte n0 = -1, n1 = -1, n2 = -1, n3 = -1;
				while (pos < dataLength && n0 == -1) {
					n0 = decodeByte(encodedData[pos++]);
				}
				while (pos < dataLength && n1 == -1) {
					n1 = decodeByte(encodedData[pos++]);
				}
				while (pos < dataLength && n2 == -1) {
					n2 = decodeByte(encodedData[pos++]);
				}
				while (pos < dataLength && n3 == -1) {
					n3 = decodeByte(encodedData[pos++]);
				}
				data[dataPos++] = (byte)(n0 << 2 | n1 >> 4);
				data[dataPos++] = (byte)(((n1 & 0xf) << 4) | ((n2 >> 2) & 0xf));
				data[dataPos++] = (byte)(n2 << 6 | n3);
			}
			outputStream.write(data, 0, dataPos);
		} finally {
			outputStream.close();
		}
	}

	@Override
	public final InputStream inputStream() {
		try {
			decode();
			return new FileInputStream(new File(decodedFileName()));
		} catch (IOException e) {
			return null;
		}
	}
}
