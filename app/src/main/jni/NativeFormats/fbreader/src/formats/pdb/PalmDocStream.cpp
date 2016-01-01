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
#include <algorithm>

#include <ZLFile.h>

#include "PalmDocStream.h"
#include "DocDecompressor.h"
#include "HuffDecompressor.h"

PalmDocStream::PalmDocStream(const ZLFile &file) : PalmDocLikeStream(file) {
}

PalmDocStream::~PalmDocStream() {
	close();
}

bool PalmDocStream::processRecord() {
	const size_t currentOffset = recordOffset(myRecordIndex);
	if (currentOffset < myBase->offset()) {
	    return false;
	}
	myBase->seek(currentOffset, true);
	const size_t nextOffset = recordOffset(myRecordIndex + 1);
	if (nextOffset < currentOffset) {
	    return false;
	}
	const unsigned short recordSize = nextOffset - currentOffset;
	switch (myCompressionVersion) {
		case 17480://'DH'	// HuffCDic compression
			myBufferLength = myHuffDecompressorPtr->decompress(*myBase, myBuffer, recordSize, myMaxRecordSize);
			//if (myHuffDecompressorPtr->error()) {
			//	myErrorCode = ERROR_UNKNOWN;
			//}
			break;
		case 2:				// PalmDoc compression
			myBufferLength = DocDecompressor().decompress(*myBase, myBuffer, recordSize, myMaxRecordSize);
			break;
		case 1:				// No compression
			myBufferLength = myBase->read(myBuffer, std::min(recordSize, myMaxRecordSize));
			break;
	}
	myBufferOffset = 0;
	return true;
}

bool PalmDocStream::processZeroRecord() {
	// Uses with offset presetting to zero record offset value
	myCompressionVersion = PdbUtil::readUnsignedShort(*myBase); // myBase offset: ^ + 2
	switch (myCompressionVersion) {
		case 1:
		case 2:
		case 17480:
			break;
		default:
			myErrorCode = ERROR_COMPRESSION;
			return false;
	}
	myBase->seek(2, false);									// myBase offset: ^ + 4
	myTextLength = PdbUtil::readUnsignedLongBE(*myBase); 	// myBase offset: ^ + 8
	myTextRecordNumber = PdbUtil::readUnsignedShort(*myBase); 	// myBase offset: ^ + 10

	unsigned short endSectionIndex = header().Offsets.size();
	myMaxRecordIndex = std::min(myTextRecordNumber, (unsigned short)(endSectionIndex - 1));
	//TODO Insert in this point error message about uncompatible records and numRecords from Header

	myMaxRecordSize = PdbUtil::readUnsignedShort(*myBase); 	// myBase offset: ^ + 12
	if (myCompressionVersion == 17480) {
		myMaxRecordSize *= 2;
	}
	if (myMaxRecordSize == 0) {
		myErrorCode = ERROR_UNKNOWN;
		return false;
	}

	/*
	std::cerr << "PalmDocStream::processRecord0():\n";
	std::cerr << "PDB header indentificator            : " << header().Id << "\n";
	std::cerr << "PDB file system: sizeof opened       : " << myBaseSize << "\n";
	std::cerr << "PDB header/record[0] max index       : " << myMaxRecordIndex << "\n";
	std::cerr << "PDB record[0][0..2] compression      : " << myCompressionVersion << "\n";
	std::cerr << "PDB record[0][2..4] spare            : " << mySpare << "\n";
	std::cerr << "PDB record[0][4..8] text length      : " << myTextLength << "\n";
	std::cerr << "PDB record[0][8..10] text records    : " << myTextRecords << "\n";
	std::cerr << "PDB record[0][10..12] max record size: " << myMaxRecordSize << "\n";
	*/

	if (header().Id == "BOOKMOBI") {
		const unsigned short encrypted = PdbUtil::readUnsignedShort(*myBase); 		// myBase offset: ^ + 14
		if (encrypted) { 										//Always = 2, if encrypted
			myErrorCode = ERROR_ENCRYPTION;
			return false;
		}
	} else {
		myBase->seek(2, false);
	}
	myBase->seek(94, false);
	myImageStartIndex = PdbUtil::readUnsignedLongBE(*myBase);

	if (myCompressionVersion == 17480) {
		unsigned long mobiHeaderLength;
		unsigned long huffSectionIndex;
		unsigned long huffSectionNumber;
		unsigned long extraFlags = 0;
		unsigned long initialOffset = header().Offsets[0];

		myBase->seek(initialOffset + 20, true); 										// myBase offset: ^ + 20
		mobiHeaderLength = PdbUtil::readUnsignedLongBE(*myBase); 		// myBase offset: ^ + 24

		myBase->seek(initialOffset + 112, true); 								// myBase offset: ^ + 112
		huffSectionIndex = PdbUtil::readUnsignedLongBE(*myBase); 		// myBase offset: ^ + 116
		huffSectionNumber = PdbUtil::readUnsignedLongBE(*myBase);		// myBase offset: ^ + 120

		if (16 + mobiHeaderLength >= 244) {
			myBase->seek(initialOffset + 240, true); 							// myBase offset: ^ + 240
			extraFlags = PdbUtil::readUnsignedLongBE(*myBase);			// myBase offset: ^ + 244
		}
		/*
		std::cerr << "mobi header length: " <<  mobiHeaderLength << "\n";
		std::cerr << "Huff's start record  : " << huffSectionIndex << " from " << endSectionIndex - 1 << "\n";
		std::cerr << "Huff's records number: " << huffSectionNumber << "\n";
		std::cerr << "Huff's extraFlags    : " << extraFlags << "\n";
		*/
		const unsigned long endHuffSectionIndex = huffSectionIndex + huffSectionNumber;
		if (endHuffSectionIndex > endSectionIndex || huffSectionNumber <= 1) {
			myErrorCode = ERROR_COMPRESSION;
			return false;
		}
		const unsigned long endHuffDataOffset = recordOffset(endHuffSectionIndex);
		std::vector<unsigned long>::const_iterator beginHuffSectionOffsetIt = header().Offsets.begin() + huffSectionIndex;
		// point to first Huff section
		std::vector<unsigned long>::const_iterator endHuffSectionOffsetIt =	header().Offsets.begin() + endHuffSectionIndex;
		// point behind last Huff section

		myHuffDecompressorPtr = new HuffDecompressor(*myBase, beginHuffSectionOffsetIt, endHuffSectionOffsetIt, endHuffDataOffset, extraFlags);
		myBase->seek(initialOffset + 14, true);									// myBase offset: ^ + 14
	}
	return true;
}

bool PalmDocStream::hasExtraSections() const {
	return myMaxRecordIndex < header().Offsets.size() - 1;
}

std::pair<int,int> PalmDocStream::imageLocation(const PdbHeader &header, int index) const {
	index += myImageStartIndex;
	int recordNumber = header.Offsets.size();
	if (index > recordNumber - 1) {
		return std::make_pair(-1, -1);
	} else {
		int start = header.Offsets[index];
		int end = (index < recordNumber - 1) ?
			header.Offsets[index + 1] : myBase->offset();
		return std::make_pair(start, end - start);
	}
}

PalmDocContentStream::PalmDocContentStream(const ZLFile &file) : PalmDocStream(file) {
}

size_t PalmDocContentStream::sizeOfOpened() {
	return myTextLength;
}

PalmDocCssStream::PalmDocCssStream(const ZLFile &file) : PalmDocStream(file) {
}

bool PalmDocCssStream::open() {
	if (!PalmDocStream::open()) {
		return false;
	}
	seek(myTextLength, false);
	if (PalmDocStream::offset() < myTextLength) {
		close();
		return false;
	}
	return true;
}

size_t PalmDocCssStream::offset() const {
	const size_t o = PalmDocStream::offset();
	return o <= myTextLength ? 0 : o - myTextLength;
}

size_t PalmDocCssStream::sizeOfOpened() {
	return (size_t)((1 << 31) - 1);
}
