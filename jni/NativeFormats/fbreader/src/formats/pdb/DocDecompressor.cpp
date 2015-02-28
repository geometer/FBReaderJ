/*
 * Copyright (C) 2004-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#include <cstring>

#include <ZLInputStream.h>

#include "DocDecompressor.h"

static unsigned char TOKEN_CODE[256] = {
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
	2, 2, 2, 2,		2, 2, 2, 2,		2, 2, 2, 2,		2, 2, 2, 2,
};

size_t DocDecompressor::decompress(ZLInputStream &stream, char *targetBuffer, size_t compressedSize, size_t maxUncompressedSize) {
	const unsigned char *sourceBuffer = new unsigned char[compressedSize];
	const unsigned char *sourceBufferEnd = sourceBuffer + compressedSize;
	const unsigned char *sourcePtr = sourceBuffer;

	unsigned char *targetBufferEnd = (unsigned char*)targetBuffer + maxUncompressedSize;
	unsigned char *targetPtr = (unsigned char*)targetBuffer;

	if (stream.read((char*)sourceBuffer, compressedSize) == compressedSize) {
		unsigned char token;
		unsigned short copyLength, N, shift;
		unsigned char *shifted;

		while ((sourcePtr < sourceBufferEnd) && (targetPtr < targetBufferEnd)) {
			token = *(sourcePtr++);
			switch (TOKEN_CODE[token]) {
				case 0:
					*(targetPtr++) = token;
					break;
				case 1:
					if ((sourcePtr + token > sourceBufferEnd) || (targetPtr + token > targetBufferEnd)) {
						goto endOfLoop;
					}
					memcpy(targetPtr, sourcePtr, token);
					sourcePtr += token;
					targetPtr += token;
					break;
				case 2:
					if (targetPtr + 2 > targetBufferEnd) {
						goto endOfLoop;
					}
					*(targetPtr++) = ' ';
					*(targetPtr++) = token ^ 0x80;
					break;
				case 3:
					if (sourcePtr + 1 > sourceBufferEnd) {
						goto endOfLoop;
					}
					N = 256 * token + *(sourcePtr++);
					copyLength = (N & 7) + 3;
					if (targetPtr + copyLength > targetBufferEnd) {
						goto endOfLoop;
					}
					shift = (N & 0x3fff) / 8;
					shifted = targetPtr - shift;
					if ((char*)shifted >= targetBuffer) {
						for (short i = 0; i < copyLength; i++) {
							*(targetPtr++) = *(shifted++);
						}
					}
					break;
			}
		}
	}
endOfLoop:

	delete[] sourceBuffer;
	return targetPtr - (unsigned char*)targetBuffer;
}
