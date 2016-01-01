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

#include "OleUtil.h"
#include "OleMainStream.h"

#include "DocInlineImageReader.h"

DocInlineImageReader::DocInlineImageReader(shared_ptr<OleStream> dataStream) :
	myDataStream(dataStream) {
}

ZLFileImage::Blocks DocInlineImageReader::getImagePieceInfo(unsigned int dataPos) {
	if (myDataStream.isNull()) {
		return ZLFileImage::Blocks();
	}
	if (!myDataStream->seek(dataPos, true)) {
		return ZLFileImage::Blocks();
	}

	//reading PICF structure (see p. 421 [MS-DOC])
	unsigned int picfHeaderSize = 4 + 2 + 8; //record length, headerLength and storage format
	char headerBuffer[picfHeaderSize];
	if (myDataStream->read(headerBuffer, picfHeaderSize) != picfHeaderSize) {
		return ZLFileImage::Blocks();
	}
	unsigned int length = OleUtil::getU4Bytes(headerBuffer, 0);
	unsigned int headerLength = OleUtil::getU2Bytes(headerBuffer, 4);
	unsigned int formatType = OleUtil::getU2Bytes(headerBuffer, 6);

	if (formatType != 0x0064) { //external link to some file; see p.394 [MS-DOC]
		//TODO implement
		return ZLFileImage::Blocks();
	}
	if (headerLength >= length) {
		return ZLFileImage::Blocks();
	}

	//reading OfficeArtInlineSpContainer structure; see p.421 [MS-DOC] and p.56 [MS-ODRAW]
	if (!myDataStream->seek(headerLength - picfHeaderSize, false)) {  //skip header
		return ZLFileImage::Blocks();
	}

	char buffer[8]; //for OfficeArtRecordHeader structure; see p.69 [MS-ODRAW]
	bool found = false;
	unsigned int curOffset = 0;
	for (curOffset = headerLength; !found && curOffset + 8 <= length; curOffset += 8) {
		if (myDataStream->read(buffer, 8) != 8) {
			return ZLFileImage::Blocks();
		}
		unsigned int recordInstance = OleUtil::getU2Bytes(buffer, 0) >> 4;
		unsigned int recordType = OleUtil::getU2Bytes(buffer, 2);
		unsigned int recordLen = OleUtil::getU4Bytes(buffer, 4);

		switch (recordType) {
			case 0xF000: case 0xF001: case 0xF002: case 0xF003: case 0xF004: case 0xF005:
				break;
			case 0xF007:
				{
					myDataStream->seek(33, false);
					char tmpBuf[1];
					myDataStream->read(tmpBuf, 1);
					unsigned int nameLength = OleUtil::getU1Byte(tmpBuf, 0);
					myDataStream->seek(nameLength * 2 + 2, false);
					curOffset += 33 + 1 + nameLength * 2 + 2;
				}
				break;
			case 0xF008:
				myDataStream->seek(8, false);
				curOffset += 8;
				break;
			case 0xF009:
				myDataStream->seek(16, false);
				curOffset += 16;
				break;
			case 0xF006: case 0xF00A: case 0xF00B: case 0xF00D: case 0xF00E: case 0xF00F: case 0xF010: case 0xF011: case 0xF122:
				myDataStream->seek(recordLen, false);
				curOffset += recordLen;
				break;
			case OleMainStream::IMAGE_EMF:
			case OleMainStream::IMAGE_WMF:
			case OleMainStream::IMAGE_PICT:
				//TODO implement
				return ZLFileImage::Blocks();
			case OleMainStream::IMAGE_JPEG:
			case OleMainStream::IMAGE_JPEG2:
				myDataStream->seek(17, false);
				curOffset += 17;
				if (recordInstance == 0x46B || recordInstance == 0x6E3) {
					myDataStream->seek(16, false);
					curOffset += 16;
				}
				found = true;
				break;
			case OleMainStream::IMAGE_PNG:
				myDataStream->seek(17, false);
				curOffset += 17;
				if (recordInstance == 0x6E1) {
					myDataStream->seek(16, false);
					curOffset += 16;
				}
				found = true;
				break;
			case OleMainStream::IMAGE_DIB: // DIB = BMP without 14-bytes header
				myDataStream->seek(17, false);
				curOffset += 17;
				if (recordInstance == 0x7A9) {
					myDataStream->seek(16, false);
					curOffset += 16;
				}
				found = true;
				break;
			case OleMainStream::IMAGE_TIFF:
				myDataStream->seek(17, false);
				curOffset += 17;
				if (recordInstance == 0x6E5) {
					myDataStream->seek(16, false);
					curOffset += 16;
				}
				found = true;
				break;
			case 0xF00C:
			default:
				return ZLFileImage::Blocks();
			}
	}

	if (!found) {
		return ZLFileImage::Blocks();
	}
	return myDataStream->getBlockPieceInfoList(dataPos + curOffset, length - curOffset);
}
