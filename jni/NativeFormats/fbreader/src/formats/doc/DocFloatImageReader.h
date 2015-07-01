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

#ifndef __DOCFLOATIMAGEREADER_H__
#define __DOCFLOATIMAGEREADER_H__

#include <ZLFileImage.h>

class DocFloatImageReader {

public:
	struct BlipStoreEntry { // see p.68 [MS-ODRAW]
		unsigned int size; // size of blip in stream
		unsigned int referenceCount; // (cRef) reference count for the the blip
		unsigned int offsetInDelay; // foDelay, file offset in the delay stream
	};

	struct Blip { //see p.59, p63-66 [MS-ODRAW]
		BlipStoreEntry storeEntry;
		unsigned int type;
		ZLFileImage::Blocks blocks;
	};

	struct FSP { //see p.76-77 [MS-ODRAW]
		unsigned int shapeId; //spid
	};

	struct FOPTE { //see p.98 and p.32 [MS-ODRAW]
		unsigned int pId; //pid
		bool isBlipId; //fBid
		bool isComplex; //fComplex
		unsigned int value; //op
	};

	struct FSPContainer { //see p.53-55 [MS-ODRAW]
		FSP fsp;
		std::vector<FOPTE> fopte;
	};

	struct OfficeArtContent { //see p.405-406 [MS-DOC]
		std::vector<Blip> blips; //retrieved from OfficeArtDggContainer
		std::vector<FSPContainer> FSPs; //retrieved from OfficeArtDgContainer
	};

	struct RecordHeader { //see p.26 [MS-ODRAW]
		unsigned int version;
		unsigned int instance;
		unsigned int type;
		unsigned int length;
	};

public:
	DocFloatImageReader(unsigned int off, unsigned int len, shared_ptr<OleStream> tableStream, shared_ptr<OleStream> mainStream);

public:
	void readAll();

	ZLFileImage::Blocks getBlocksForShapeId(unsigned int shapeId) const;

private:
	static unsigned int readRecordHeader(RecordHeader &header, shared_ptr<OleStream> stream);
	static unsigned int readDggContainer(OfficeArtContent &item, unsigned int length, shared_ptr<OleStream> stream, shared_ptr<OleStream> mainStream);

	static unsigned int readBStoreContainer(OfficeArtContent &item, unsigned int length, shared_ptr<OleStream> stream, shared_ptr<OleStream> mainStream);
	static unsigned int readBStoreContainerFileBlock(Blip &blip, shared_ptr<OleStream> stream, shared_ptr<OleStream> mainStream);
	static unsigned int readBlip(Blip &blip, const RecordHeader &header, shared_ptr<OleStream> stream);
	static unsigned int readFBSE(BlipStoreEntry &fbse, shared_ptr<OleStream> stream);

	static unsigned int readFOPTE(FOPTE &fopte, shared_ptr<OleStream> stream);
	static unsigned int readArrayFOPTE(std::vector<FOPTE> &fopte, unsigned int length, shared_ptr<OleStream> stream);
	static unsigned int readFSP(FSP &fsp, shared_ptr<OleStream> stream);
	static unsigned int readSpContainter(FSPContainer &item, unsigned int length, shared_ptr<OleStream> stream);
	static unsigned int readSpgrContainer(OfficeArtContent &item, unsigned int length, shared_ptr<OleStream> stream);
	static unsigned int readDgContainer(OfficeArtContent &item,  unsigned int length, shared_ptr<OleStream> stream);

	static unsigned int skipRecord(const RecordHeader &header, shared_ptr<OleStream> stream);

	static unsigned int read1Byte(shared_ptr<OleStream> stream);
	static unsigned int read2Bytes(shared_ptr<OleStream> stream);
	static unsigned int read4Bytes(shared_ptr<OleStream> stream);

private:
	shared_ptr<OleStream> myTableStream;
	shared_ptr<OleStream> myMainStream;
	unsigned int myOffset;
	unsigned int myLength;

	OfficeArtContent myItem;
};

#endif /* __DOCFLOATIMAGEREADER_H__ */
