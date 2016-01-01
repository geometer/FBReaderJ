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

#ifndef __PDBREADER_H__
#define __PDBREADER_H__

#include <vector>

#include <shared_ptr.h>
#include <ZLInputStream.h>

//class BookModel;

class PdbUtil {

public:
	static unsigned short readUnsignedShort(ZLInputStream &stream);
	static unsigned long readUnsignedLongBE(ZLInputStream &stream);
	static unsigned long readUnsignedLongLE(ZLInputStream &stream);
};

struct PdbHeader {
	std::string DocName;
	unsigned short Flags;
	std::string Id;
	std::vector<unsigned long> Offsets;

	bool read(shared_ptr<ZLInputStream> stream);
};

struct PdbRecord0 {
	unsigned short CompressionType;		//[0..2]	PalmDoc, Mobipocket, Ereader:version
	unsigned short Spare;				//[2..4]	PalmDoc, Mobipocket
	unsigned long  TextLength;			//[4..8]	PalmDoc, Mobipocket
	unsigned short TextRecords;			//[8..10]	PalmDoc, Mobipocket
	unsigned short MaxRecordSize; 		//[10..12]	PalmDoc, Mobipocket
	unsigned short NontextOffset;		//[12..14]	Ereader
	unsigned short NontextOffset2;		//[14..16]	Ereader  //PalmDoc, Mobipocket: encrypted - there is conflict !!!!
	
	unsigned long  MobipocketID;		//[16..20]	Mobipocket
	unsigned long  MobipocketHeaderSize;//[20..24]	Mobipocket
	unsigned long  Unknown24;			//[24..28]
	unsigned short FootnoteRecs;		//[28..30]	Ereader
	unsigned short SidebarRecs;			//[30..32]	Ereader

// Following fields are specific for EReader pdb document specification
	
	unsigned short BookmarkOffset;		//[32..34] 
	unsigned short Unknown34;			//[34..36]
	unsigned short NontextOffset3;		//[36..38]
	unsigned short Unknown38;			//[38..40]
	unsigned short ImagedataOffset;		//[40..42]
	unsigned short ImagedataOffset2;	//[42..44]
	unsigned short MetadataOffset;		//[44..46]
	unsigned short MetadataOffset2;		//[46..48]
	unsigned short FootnoteOffset;		//[48..50]
	unsigned short SidebarOffset;		//[50..52]
	unsigned short LastDataOffset;		//[52..54]
	unsigned short Unknown54;			//[54..56]

	bool read(shared_ptr<ZLInputStream> stream);
//private:
//	static bool readNumberBE(unsigned char* buffer, size_t offset, size_t size); 
};

#endif /* __PDBREADER_H__ */
