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

#include <ZLLogger.h>

#include "OleUtil.h"
#include "OleStream.h"
#include "OleMainStream.h"

#include "DocFloatImageReader.h"

DocFloatImageReader::DocFloatImageReader(unsigned int off, unsigned int len, shared_ptr<OleStream> tableStream, shared_ptr<OleStream> mainStream) :
	myTableStream(tableStream),
	myMainStream(mainStream),
	myOffset(off),
	myLength(len) {
}

void DocFloatImageReader::readAll() {
	//OfficeArtContent structure is described at p.405-406 [MS-DOC]
	if (!myTableStream->seek(myOffset, true)) {
		ZLLogger::Instance().println("DocPlugin", "problems with reading float images");
		return;
	}

	unsigned int count = 0;

	RecordHeader header;
	while (count < myLength)  {
		count += readRecordHeader(header, myTableStream);
		switch (header.type) {
			case 0xF000:
				count += readDggContainer(myItem, header.length, myTableStream, myMainStream);
				break;
			case 0xF002:
				count += readDgContainer(myItem, header.length, myTableStream);
				break;
			default:
				return;
				break;
		}
	}
}

ZLFileImage::Blocks DocFloatImageReader::getBlocksForShapeId(unsigned int shapeId) const {
	FSPContainer container;
	bool found = false;
	for (std::size_t i = 0; !found && i < myItem.FSPs.size(); ++i) {
		if (myItem.FSPs.at(i).fsp.shapeId == shapeId) {
			found = true;
			container = myItem.FSPs.at(i);
		}
	}

	if (!found || container.fopte.empty()) {
		return ZLFileImage::Blocks();
	}

	for (std::size_t i = 0; i < container.fopte.size(); ++i) {
		const FOPTE &fopte = container.fopte.at(i);
		if (fopte.pId == 0x0104 && !fopte.isComplex) { //0x0104 specifies the BLIP, see p.420 [MS-ODRAW]
			if (fopte.value <= myItem.blips.size() && fopte.value > 0) {
				Blip blip = myItem.blips.at(fopte.value - 1);
				return blip.blocks;
			}
		}
	}
	return ZLFileImage::Blocks();
}

unsigned int DocFloatImageReader::readRecordHeader(RecordHeader &header, shared_ptr<OleStream> stream) {
	//OfficeArtRecordHeader structure is described at p.26 [MS-ODRAW]
	char buffer[8];
	stream->read(buffer, 8);
	unsigned int temp = OleUtil::getU2Bytes(buffer, 0);
	header.version = temp & 0x000F;
	header.instance = temp >> 4;
	header.type = OleUtil::getU2Bytes(buffer, 2);
	header.length = OleUtil::getU4Bytes(buffer, 4);
	return 8;
}

unsigned int DocFloatImageReader::readDggContainer(OfficeArtContent &item, unsigned int length,  shared_ptr<OleStream> stream, shared_ptr<OleStream> mainStream) {
	//OfficeArtDggContainer structure is described at p.50 [MS-ODRAW]
	RecordHeader header;
	unsigned int count = 0;

	while (count < length) {
		count += readRecordHeader(header, stream);
		switch (header.type) {
			case 0xF001:
				count += readBStoreContainer(item, header.length, stream, mainStream);
				break;
			default:
				count += skipRecord(header, stream);
				break;
		}
	}

	stream->seek(1, false); //skipping dgglbl (see p.406 [MS-DOC])
	++count;

	return count;
}

unsigned int DocFloatImageReader::readBStoreContainer(OfficeArtContent &item, unsigned int length, shared_ptr<OleStream> stream, shared_ptr<OleStream> mainStream) {
	//OfficeArtBStoreContainer structure is described at p.58 [MS-ODRAW]
	RecordHeader header;
	unsigned int count = 0;
	while (count < length) {
		count += readRecordHeader(header, stream);
		switch (header.type) {
			case 0xF007:
				{
					Blip blip;
					count += readBStoreContainerFileBlock(blip, stream, mainStream);
					item.blips.push_back(blip);
				}
				break;
			default:
				count += skipRecord(header, stream);
				break;
		}
	}
	return count;
}

unsigned int DocFloatImageReader::skipRecord(const RecordHeader &header, shared_ptr<OleStream> stream) {
	stream->seek(header.length, false);
	return header.length;
}

unsigned int DocFloatImageReader::readBStoreContainerFileBlock(Blip &blip, shared_ptr<OleStream> stream, shared_ptr<OleStream> mainStream) {
	//OfficeArtBStoreContainerFileBlock structure is described at p.59 [MS-ODRAW]
	unsigned int count = readFBSE(blip.storeEntry, stream);
	if (blip.storeEntry.offsetInDelay != (unsigned int)-1) {
		if (mainStream->seek(blip.storeEntry.offsetInDelay, true)) { //see p.70 [MS-ODRAW]
			//TODO maybe we should stop reading float images here
			ZLLogger::Instance().println("DocPlugin", "DocFloatImageReader: problems with seeking for offset");
			return count;
		}
	}
	RecordHeader header;
	unsigned int count2 = readRecordHeader(header, mainStream);
	switch (header.type) {
		case OleMainStream::IMAGE_WMF:
		case OleMainStream::IMAGE_EMF:
		case OleMainStream::IMAGE_PICT:
			count2 += skipRecord(header, mainStream);
			break;
		case OleMainStream::IMAGE_JPEG:
		case OleMainStream::IMAGE_JPEG2:
		case OleMainStream::IMAGE_PNG:
		case OleMainStream::IMAGE_DIB:
		case OleMainStream::IMAGE_TIFF:
			count2 += readBlip(blip, header, mainStream);
			break;
	}
	blip.type = header.type;
	return count;
}

unsigned int DocFloatImageReader::readBlip(Blip &blip, const RecordHeader &header, shared_ptr<OleStream> stream) {
	//OfficeArtBlip structure is described at p.60-66 [MS-ODRAW]
	stream->seek(16, false); //skipping rgbUid1
	unsigned int count = 16;

	bool addField = false;
	switch (header.type) {
		case OleMainStream::IMAGE_PNG:
			if (header.instance == 0x6E1) {
				addField = true;
			}
			break;
		case OleMainStream::IMAGE_JPEG:
		case OleMainStream::IMAGE_JPEG2:
			if (header.instance == 0x46B || header.instance == 0x6E3) {
				addField = true;
			}
			break;
		case OleMainStream::IMAGE_DIB:
			if (header.instance == 0x7A9) {
				addField = true;
			}
		case OleMainStream::IMAGE_TIFF:
			if (header.instance == 0x6E5) {
				addField = true;
			}
			break;
	}

	if (addField) {
		stream->seek(16, false); //skipping rgbUid2
		count += 16;
	}
	stream->seek(1, false); //skipping tag
	count += 1;

	blip.blocks = stream->getBlockPieceInfoList(stream->offset(), header.length - count);
	count += header.length;
	return count;
}

unsigned int DocFloatImageReader::readFBSE(BlipStoreEntry &fbse, shared_ptr<OleStream> stream) {
	//OfficeArtFBSE structure is described at p.68 [MS-ODRAW]
	stream->seek(2, false); //skipping btWin32 and btMacOS
	stream->seek(16, false); //skipping rgbUid
	stream->seek(2, false); //skipping tag
	fbse.size = read4Bytes(stream);
	fbse.referenceCount = read4Bytes(stream);
	fbse.offsetInDelay = read4Bytes(stream);
	stream->seek(1, false); //skipping unused value
	unsigned int lengthName = read1Byte(stream); //if it should be multiplied on 2?
	stream->seek(2, false); // skipping unused values
	if (lengthName > 0) {
		stream->seek(lengthName, false); //skipping nameData
	}
	return 36 + lengthName;
}

unsigned int DocFloatImageReader::readDgContainer(OfficeArtContent &item, unsigned int length, shared_ptr<OleStream> stream) {
	//OfficeArtDgContainer structure is described at p.52 [MS-ODRAW]
	unsigned int count = 0;

	RecordHeader header;
	while (count < length) {
		count += readRecordHeader(header, stream);
		switch (header.type) {
			case 0xF008: //skip OfficeArtFDG record, p. 82 [MS-ODRAW]
				stream->seek(8, false);
				count += 8;
				break;
			case 0xF003:
				count += readSpgrContainer(item, header.length, stream);
				break;
			case 0xF004:
				{
					FSPContainer fspContainer;
					count += readSpContainter(fspContainer, header.length, stream);
					item.FSPs.push_back(fspContainer);
				}
				break;
			default:
				count += skipRecord(header, stream);
				break;
		}
	}
	return count;
}

unsigned int DocFloatImageReader::readSpgrContainer(OfficeArtContent &item, unsigned int length, shared_ptr<OleStream> stream) {
	//OfficeArtSpgrContainer structure is described at p.56 [MS-ODRAW]
	unsigned count = 0;
	RecordHeader header;
	while (count < length) {
		count += readRecordHeader(header, stream);
		switch (header.type) {
			case 0xF003:
				count += readSpgrContainer(item, header.length, stream);
				break;
			case 0xF004:
				{
					FSPContainer fspContainer;
					count += readSpContainter(fspContainer, header.length, stream);
					item.FSPs.push_back(fspContainer);
				}
				break;
			default:
				count += skipRecord(header, stream);
			break;
		}
	}
	return count;
}

unsigned int DocFloatImageReader::readSpContainter(FSPContainer &item, unsigned int length, shared_ptr<OleStream> stream) {
	//OfficeArtSpContainter structure is described at p.53-55 [MS-ODRAW]
	RecordHeader header;
	unsigned int count = 0;
	while (count < length) {
		count += readRecordHeader(header, stream);
		switch (header.type) {
			case 0xF009: //skip OfficeArtFSPGR record, p.74 [MS-ODRAW]
				stream->seek(16, false);
				count += 16;
				break;
			case 0xF00A:
				count += readFSP(item.fsp, stream);
				break;
			case 0xF00B:
				count += readArrayFOPTE(item.fopte, header.length, stream);
				break;
			case 0xF00E: //OfficeArtAnchor
			case 0xF00F: //OfficeArtChildAnchor, p.75 [MS-ODRAW]
			case 0xF010: //OfficeArtClientAnchor
				stream->seek(4, false);
				count += 4;
				break;
			case 0xF00C:
			case 0xF11F:
			case 0xF11D:
				break;
			default:
				count += skipRecord(header, stream);
				break;
		}
	}
	return count;
}

unsigned int DocFloatImageReader::readFSP(FSP &fsp, shared_ptr<OleStream> stream) {
	//OfficeArtFSP structure is described at p.76 [MS-ODRAW]
	fsp.shapeId = read4Bytes(stream);
	stream->seek(4, false);
	return 8;
}

unsigned int DocFloatImageReader::readArrayFOPTE(std::vector<FOPTE> &fopteArray,unsigned int length, shared_ptr<OleStream> stream) {
	//OfficeArtRGFOPTE structure is described at p.98 [MS-ODRAW]
	unsigned int count = 0;
	while (count < length) {
		FOPTE fopte;
		count += readFOPTE(fopte, stream);
		fopteArray.push_back(fopte);
	}
	for (std::size_t i = 0; i < fopteArray.size(); ++i) {
		if (fopteArray.at(i).isComplex) {
			stream->seek(fopteArray.at(i).value, false);
			count += fopteArray.at(i).value;
		}
	}
	return count;
}

unsigned int DocFloatImageReader::readFOPTE(FOPTE &fopte, shared_ptr<OleStream> stream) {
	//OfficeArtFOPTE structure is described at p.32 [MS-ODRAW]
	unsigned int dtemp;
	dtemp = read2Bytes(stream);
	fopte.pId = (dtemp & 0x3fff);
	fopte.isBlipId = ((dtemp & 0x4000) >> 14) == 0x1;
	fopte.isComplex = ((dtemp & 0x8000) >> 15) == 0x1;
	fopte.value = read4Bytes(stream);
	return 6;
}

unsigned int DocFloatImageReader::read1Byte(shared_ptr<OleStream> stream) {
	char b[1];
	if (stream->read(b, 1) != 1) {
		return 0;
	}
	return OleUtil::getU1Byte(b, 0);
}

unsigned int DocFloatImageReader::read2Bytes(shared_ptr<OleStream> stream) {
	char b[2];
	if (stream->read(b, 2) != 2) {
		return 0;
	}
	return OleUtil::getU2Bytes(b, 0);
}

unsigned int DocFloatImageReader::read4Bytes(shared_ptr<OleStream> stream) {
	char b[4];
	if (stream->read(b, 4) != 4) {
		return 0;
	}
	return OleUtil::getU4Bytes(b, 0);
}
