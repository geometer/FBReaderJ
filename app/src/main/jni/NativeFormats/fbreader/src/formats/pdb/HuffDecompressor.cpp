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

#include "PdbReader.h"
#include "BitReader.h"
#include "HuffDecompressor.h"

HuffDecompressor::HuffDecompressor(
	ZLInputStream& stream,
	const std::vector<unsigned long>::const_iterator beginIt,
	const std::vector<unsigned long>::const_iterator endIt,
	const unsigned long endHuffDataOffset, const unsigned long extraFlags) : myExtraFlags(extraFlags), myErrorCode(ERROR_NONE) {
	const unsigned long huffHeaderOffset = *beginIt;
	const unsigned long huffRecordsNumber = endIt - beginIt;
	const unsigned long huffDataOffset = *(beginIt + 1);

	stream.seek(huffHeaderOffset, true);
	stream.seek(16, false);
	const unsigned long cacheTableOffset = PdbUtil::readUnsignedLongBE(stream);
	const unsigned long baseTableOffset = PdbUtil::readUnsignedLongBE(stream);

	myCacheTable = new unsigned long[256];
	stream.seek(huffHeaderOffset + cacheTableOffset, true);
	for (size_t i = 0; i < 256; ++i) {
		myCacheTable[i] = PdbUtil::readUnsignedLongLE(stream); //LE
	}

	myBaseTable = new unsigned long[64];
	stream.seek(huffHeaderOffset + baseTableOffset, true);
	for (size_t i = 0; i < 64; ++i) {
		myBaseTable[i] = PdbUtil::readUnsignedLongLE(stream); //LE
	}

	stream.seek(huffDataOffset + 12, true);
	myEntryBits = PdbUtil::readUnsignedLongBE(stream);

	size_t huffDataSize = endHuffDataOffset - huffDataOffset;
	myData = new unsigned char[huffDataSize];
	stream.seek(huffDataOffset, true);
	if (huffDataSize == stream.read((char*)myData, huffDataSize)) {
		myDicts = new unsigned char*[huffRecordsNumber - 1];
		for(size_t i = 0; i < huffRecordsNumber - 1; ++i) {
			size_t shift = *(beginIt + i + 1) - huffDataOffset;
			myDicts[i] = myData + shift;
		}
	} else {
		myErrorCode = ERROR_CORRUPTED_FILE;
	}

	myTargetBuffer = 0;
	myTargetBufferEnd = 0;
	myTargetBufferPtr = 0;
}

HuffDecompressor::~HuffDecompressor() {
	delete[] myCacheTable;
	delete[] myBaseTable;
	delete[] myData;
	delete[] myDicts;
}

bool HuffDecompressor::error() const {
	return myErrorCode == ERROR_CORRUPTED_FILE;
}

size_t HuffDecompressor::decompress(ZLInputStream &stream, char *targetBuffer, size_t compressedSize, size_t maxUncompressedSize) {
	if (compressedSize == 0 || myErrorCode == ERROR_CORRUPTED_FILE) {
		return 0;
	}
	if (targetBuffer != 0) {
		unsigned char *sourceBuffer = new unsigned char[compressedSize];
		myTargetBuffer = targetBuffer;
		myTargetBufferEnd = targetBuffer + maxUncompressedSize;
		myTargetBufferPtr = targetBuffer;
		if (stream.read((char*)sourceBuffer, compressedSize) == compressedSize) {
			const size_t trailSize = sizeOfTrailingEntries(sourceBuffer, compressedSize);
			if (trailSize < compressedSize) {
				BitReader reader(sourceBuffer, compressedSize - trailSize);
				bitsDecompress(reader);
			} else {
				myErrorCode = ERROR_CORRUPTED_FILE;
			}
		}
		delete[] sourceBuffer;
	} else {
		myTargetBuffer = 0;
		myTargetBufferEnd = 0;
		myTargetBufferPtr = 0;
	}

	return myTargetBufferPtr - myTargetBuffer;
}

void HuffDecompressor::bitsDecompress(BitReader &bits, size_t depth) {
	if (depth > 32) {
		myErrorCode = ERROR_CORRUPTED_FILE;
		return;
	}

	while (bits.left()) {
		const unsigned long dw = (unsigned long)bits.peek(32);
		const unsigned long v = myCacheTable[dw >> 24];
		unsigned long codelen = v & 0x1F;
		//if ((codelen == 0) || (codelen > 32)) {
		//	return false;
		//}
		unsigned long code = dw >> (32 - codelen);
		unsigned long r = (v >> 8);
		if ((v & 0x80) == 0) {
			while (code < myBaseTable[(codelen - 1) * 2]) {
				codelen += 1;
				code = dw >> (32 - codelen);
			}
			r = myBaseTable[(codelen - 1) * 2 + 1];
		}
		r -= code;
		//if (codelen == 0) {
		//	return false;
		//}
		if (!bits.eat(codelen)) {
			break;
		}
		const unsigned long dicno = r >> myEntryBits;
		const unsigned long off1 = 16 + (r - (dicno << myEntryBits)) * 2;
		const unsigned char* dict = myDicts[dicno];							//TODO need index check
		const unsigned long off2 = 16 + dict[off1] * 256 + dict[off1 + 1];	//TODO need index check
		const unsigned long blen = dict[off2] * 256 + dict[off2 + 1];		//TODO need index check
		const unsigned char* slice = dict + off2 + 2;
		const unsigned long sliceSize = blen & 0x7fff;
		if (blen & 0x8000) {
			if (myTargetBufferPtr + sliceSize < myTargetBufferEnd) {
				memcpy(myTargetBufferPtr, slice, sliceSize);
				myTargetBufferPtr += sliceSize;
			} else {
				break;
			}
		} else {
			BitReader reader(slice, sliceSize);
			bitsDecompress(reader, depth + 1);
		}
	}
}

size_t HuffDecompressor::sizeOfTrailingEntries(unsigned char* data, size_t size) const {
	size_t num = 0;
	size_t flags = myExtraFlags >> 1;
	while (flags) {
		if (flags & 1) {
			if (num < size) {
				num += readVariableWidthIntegerBE(data, size - num);
			}
		}
		flags >>= 1;
	}
	if (myExtraFlags & 1) {
		num += (data[size - num - 1] & 0x3) + 1;
	}
	return num;
}

size_t HuffDecompressor::readVariableWidthIntegerBE(unsigned char* ptr, size_t psize) const {
	unsigned char bitsSaved = 0;
	size_t result = 0;
	while (true) {
		const unsigned char oneByte = ptr[psize - 1];
		result |= (oneByte & 0x7F) << bitsSaved;
		bitsSaved += 7;
		psize -= 1;
		if ((oneByte & 0x80) != 0 || bitsSaved >= 28 || psize == 0) {
			return result;
		}
	}
}
