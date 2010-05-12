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

final class Base64EncodedImage extends ZLSingleImage {

	private static final String ENCODED_SUFFIX = ".base64";

	private String myFileName;
	private boolean myIsDecoded;

	// mimeType string MUST be interned
	public Base64EncodedImage(String mimeType) {
		super(mimeType);
		new File(makeImagesDir()).mkdirs();
	}

	public void setData(String data) {
		myFileName = makeImagesDir() + File.separator + Integer.toHexString(data.hashCode());
		String type = mimeType();
		if (type == NetworkImage.MIME_PNG) {
			myFileName += ".png";
		} else if (type == NetworkImage.MIME_JPEG) {
			myFileName += ".jpg";
		}
		myIsDecoded = isCacheValid(new File(myFileName));
		if (myIsDecoded) {
			return;
		}
		myFileName += ENCODED_SUFFIX;
		File file = new File(myFileName);
		if (isCacheValid(file)) {
			return;
		}
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			try {
				writer.write(data, 0, data.length());
			} finally {
				writer.close();
			}
		} catch (IOException e) {
		}
	}

	private static boolean isCacheValid(File file) {
		if (file.exists()) {
			final long diff = System.currentTimeMillis() - file.lastModified();
			final long valid = 24 * 60 * 60 * 1000; // one day in milliseconds; FIXME: hardcoded const
			if (diff >= 0 && diff <= valid) {
				return true;
			}
			file.delete();
		}
		return false;
	}

	public static String makeImagesDir() {
		return NetworkImage.makeImagesDir() + File.separator + "base64";
	}

	private static byte decodeByte(byte encodedByte) {
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
			try {
				stream.read(data);
			} finally {
				stream.close();
			}
			return data;
		} catch (IOException e) {
			return null;
		}
	}

	private void decode() throws IOException {
		if (myIsDecoded) {
			return;
		}
		myIsDecoded = true;
		final String encodedFileName = myFileName;
		myFileName = myFileName.substring(0, myFileName.length() - ENCODED_SUFFIX.length());

		final File outputFile = new File(myFileName);
		if (isCacheValid(outputFile)) {
			return;
		}

		FileOutputStream outputStream = new FileOutputStream(outputFile);
		try {
			int dataLength;
			byte[] encodedData;
			final File file = new File(encodedFileName);
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
				data[dataPos++] = (byte)(n0 << 2 | n1 >> 4);
				data[dataPos++] = (byte)(((n1 & 0xf) << 4) | ((n2 >> 2) & 0xf));
				data[dataPos++] = (byte)(n2 << 6 | n3);
			}
			outputStream.write(data, 0, dataPos);
		} finally {
			outputStream.close();
		}
	}
}
