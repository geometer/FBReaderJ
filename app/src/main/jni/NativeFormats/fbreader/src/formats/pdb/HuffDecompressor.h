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

#ifndef __HUFFDECOMPRESSOR_H__
#define __HUFFDECOMPRESSOR_H__

#include <string>

class ZLInputStream;
class BitReader;

class HuffDecompressor {

public:
	HuffDecompressor(ZLInputStream& stream, 
						const std::vector<unsigned long>::const_iterator beginHuffRecordOffsetIt, 
						const std::vector<unsigned long>::const_iterator endHuffRecordOffsetIt,
						const unsigned long endHuffDataOffset, const unsigned long extraFlags);
	~HuffDecompressor();

	size_t decompress(ZLInputStream &stream, char *buffer, size_t compressedSize, size_t maxUncompressedSize);
	bool error() const;
private:
	size_t sizeOfTrailingEntries(unsigned char* data, size_t size) const;
	size_t readVariableWidthIntegerBE(unsigned char* ptr, size_t psize) const;
	void bitsDecompress(BitReader &bits, size_t depth = 0);

private:
	unsigned long myEntryBits;
	unsigned long myExtraFlags;

	unsigned long* myCacheTable;
	unsigned long* myBaseTable;
	unsigned char* myData;
	unsigned char** myDicts;

	char* myTargetBuffer;
	char* myTargetBufferEnd;
	char* myTargetBufferPtr;

	enum {
		ERROR_NONE,
		ERROR_CORRUPTED_FILE
	} myErrorCode;
};

#endif /* __HUFFDECOMPRESSOR_H__ */
