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

package org.geometerplus.fbreader.formats.pdb;

import java.io.*;

public abstract class DocDecompressor {
	private static final byte[] TOKEN_CODE = {
		0, 1, 1, 1,		1, 1, 1, 1,		1, 0, 0, 0,		0, 0, 0, 0,
		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,
		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,
		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,
		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,
		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,
		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,
		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,		0, 0, 0, 0,
		3, 3, 3, 3,		3, 3, 3, 3,		3, 3, 3, 3,		3, 3, 3, 3,
		3, 3, 3, 3,		3, 3, 3, 3,		3, 3, 3, 3,		3, 3, 3, 3,
		3, 3, 3, 3,		3, 3, 3, 3,		3, 3, 3, 3,		3, 3, 3, 3,
		3, 3, 3, 3,		3, 3, 3, 3,		3, 3, 3, 3,		3, 3, 3, 3,
		2, 2, 2, 2,		2, 2, 2, 2,		2, 2, 2, 2,		2, 2, 2, 2,
		2, 2, 2, 2,		2, 2, 2, 2,		2, 2, 2, 2,		2, 2, 2, 2,
		2, 2, 2, 2,		2, 2, 2, 2,		2, 2, 2, 2,		2, 2, 2, 2,
		2, 2, 2, 2,		2, 2, 2, 2,		2, 2, 2, 2,		2, 2, 2, 2
	};

	public static int decompress(InputStream stream, byte[] targetBuffer, int compressedSize) throws IOException {
		final int maxUncompressedSize = targetBuffer.length;
		final byte[] sourceBuffer = new byte[compressedSize];

		int sourceIndex = 0;
		int targetIndex = 0;

		if (stream.read(sourceBuffer, 0, compressedSize) == compressedSize) {
			byte token;
			int shiftedIndex;

loop:
			while ((sourceIndex < compressedSize) && (targetIndex < maxUncompressedSize)) {
				token = sourceBuffer[sourceIndex++];
				switch (TOKEN_CODE[token & 0xFF]) {
					case 0:
						targetBuffer[targetIndex++] = token;
						break;
					case 1:
						if ((sourceIndex + token > compressedSize) ||
								(targetIndex + token > maxUncompressedSize)) {
							break loop;
						}
						System.arraycopy(sourceBuffer, sourceIndex, targetBuffer, targetIndex, token);
						sourceIndex += token;
						targetIndex += token;
						break;
					case 2:
						if (targetIndex + 2 > maxUncompressedSize) {
							break loop;
						}
						targetBuffer[targetIndex++] = ' ';
						targetBuffer[targetIndex++] = (byte)(token ^ 0x80);
						break;
					case 3:
						if (sourceIndex + 1 > compressedSize) {
							break loop;
						}
						int N = 256 * (token & 0xFF) + (sourceBuffer[sourceIndex++] & 0xFF);
						int copyLength = (N & 7) + 3;
						if (targetIndex + copyLength > maxUncompressedSize) {
							break loop;
						}
						shiftedIndex = targetIndex - (N & 0x3fff) / 8;
						if (shiftedIndex >= 0) {
							for (int i = 0; i < copyLength; i++) {
								targetBuffer[targetIndex++] = targetBuffer[shiftedIndex++];
							}
						}
						break;
				}
			}
		}

		return targetIndex;
	}
}
