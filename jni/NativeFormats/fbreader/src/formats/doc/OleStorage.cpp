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

#include "OleStorage.h"
#include "OleUtil.h"

#include <cstring>

const std::size_t OleStorage::BBD_BLOCK_SIZE = 512;

OleStorage::OleStorage() {
	clear();
}

void OleStorage::clear() {
	myInputStream = 0;
	mySectorSize = 0;
	myShortSectorSize = 0;
	myStreamSize = 0;
	myRootEntryIndex = -1;

	myDIFAT.clear();
	myBBD.clear();
	mySBD.clear();
	myProperties.clear();
	myEntries.clear();
}



bool OleStorage::init(shared_ptr<ZLInputStream> stream, std::size_t streamSize) {
	clear();

	myInputStream = stream;
	myStreamSize = streamSize;
	myInputStream->seek(0, true);

	char oleBuf[BBD_BLOCK_SIZE];
	std::size_t ret = myInputStream->read(oleBuf, BBD_BLOCK_SIZE);
	if (ret != BBD_BLOCK_SIZE) {
		clear();
		return false;
	}
	static const char OLE_SIGN[] = {0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1, 0};
	if (std::strncmp(oleBuf, OLE_SIGN, 8) != 0) {
		clear();
		return false;
	}
	mySectorSize = 1 << OleUtil::getU2Bytes(oleBuf, 0x1e); //offset for value of big sector size
	myShortSectorSize = 1 << OleUtil::getU2Bytes(oleBuf, 0x20); //offset for value of small sector size

	if (readDIFAT(oleBuf) && readBBD(oleBuf) && readSBD(oleBuf) && readProperties(oleBuf) && readAllEntries()) {
		return true;
	}
	clear();
	return false;
}

bool OleStorage::readDIFAT(char *oleBuf) {
	int difatBlock = OleUtil::get4Bytes(oleBuf, 0x44); //address for first difat sector
	int difatSectorNumbers = OleUtil::get4Bytes(oleBuf, 0x48); //numbers of additional difat records

	//436 of difat records are stored in header, by offset 0x4c
	for (unsigned int i = 0; i < 436; i += 4) {
		myDIFAT.push_back(OleUtil::get4Bytes(oleBuf + 0x4c, i));
	}

	//for files > 6.78 mb we need read additional DIFAT fields
	for (int i = 0; difatBlock > 0 && i < difatSectorNumbers; ++i) {
		ZLLogger::Instance().println("DocPlugin", "Read additional data for DIFAT");
		char buffer[mySectorSize];
		myInputStream->seek(BBD_BLOCK_SIZE + difatBlock * mySectorSize, true);
		if (myInputStream->read(buffer, mySectorSize) != mySectorSize) {
			ZLLogger::Instance().println("DocPlugin", "Error read DIFAT!");
			return false;
		}
		for (unsigned int j = 0; j < (mySectorSize - 4); j += 4) {
			myDIFAT.push_back(OleUtil::get4Bytes(buffer, j));
		}
		difatBlock = OleUtil::get4Bytes(buffer, mySectorSize - 4); //next DIFAT block is pointed at the end of the sector
	}

	//removing unusable DIFAT links
	//0xFFFFFFFF means "free section"
	while (!myDIFAT.empty() && myDIFAT.back() == (int)0xFFFFFFFF) {
		myDIFAT.pop_back();
	}
	return true;
}

bool OleStorage::readBBD(char *oleBuf) {
	char buffer[mySectorSize];
	unsigned int bbdNumberBlocks = OleUtil::getU4Bytes(oleBuf, 0x2c); //number of big blocks

	if (myDIFAT.size() < bbdNumberBlocks) {
		//TODO maybe add check on myDIFAT == bbdNumberBlocks
		ZLLogger::Instance().println("DocPlugin", "Wrong number of FAT blocks value");
		return false;
	}

	for (unsigned int i = 0; i < bbdNumberBlocks; ++i) {
		int bbdSector = myDIFAT.at(i);
		if (bbdSector >= (int)(myStreamSize / mySectorSize) || bbdSector < 0) {
			ZLLogger::Instance().println("DocPlugin", "Bad BBD entry!");
			return false;
		}
		myInputStream->seek(BBD_BLOCK_SIZE + bbdSector * mySectorSize, true);
		if (myInputStream->read(buffer, mySectorSize) != mySectorSize) {
			ZLLogger::Instance().println("DocPlugin", "Error during reading BBD!");
			return false;
		}
		for (unsigned int j = 0; j < mySectorSize; j += 4) {
			myBBD.push_back(OleUtil::get4Bytes(buffer, j));
		}
	}
	return true;
}

bool OleStorage::readSBD(char *oleBuf) {
	int sbdCur = OleUtil::get4Bytes(oleBuf, 0x3c); //address of first small sector
	int sbdCount = OleUtil::get4Bytes(oleBuf, 0x40); //count of small sectors

	if (sbdCur <= 0) {
		ZLLogger::Instance().println("DocPlugin", "There's no SBD, don't read it");
		return true;
	}

	char buffer[mySectorSize];
	for (int i = 0; i < sbdCount; ++i) {
		if (i != 0) {
			if (sbdCur < 0 || (unsigned int)sbdCur >= myBBD.size()) {
				ZLLogger::Instance().println("DocPlugin", "error during parsing SBD");
				return false;
			}
			sbdCur = myBBD.at(sbdCur);
		}
		if (sbdCur <= 0) {
			break;
		}
		myInputStream->seek(BBD_BLOCK_SIZE + sbdCur * mySectorSize, true);
		if (myInputStream->read(buffer, mySectorSize) != mySectorSize) {
			ZLLogger::Instance().println("DocPlugin", "reading error during parsing SBD");
			return false;
		}
		for (unsigned int j = 0; j < mySectorSize; j += 4) {
			mySBD.push_back(OleUtil::get4Bytes(buffer, j));
		}

	}
	return true;
}

bool OleStorage::readProperties(char *oleBuf) {
	int propCur = OleUtil::get4Bytes(oleBuf, 0x30); //offset for address of sector with first property
	if (propCur < 0) {
		ZLLogger::Instance().println("DocPlugin", "Wrong first directory sector location");
		return false;
	}

	char buffer[mySectorSize];
	do {
		myInputStream->seek(BBD_BLOCK_SIZE + propCur * mySectorSize, true);
		if (myInputStream->read(buffer, mySectorSize) != mySectorSize) {
			ZLLogger::Instance().println("DocPlugin", "Error during reading properties");
			return false;
		}
		for (unsigned int j = 0; j < mySectorSize; j += 128) {
			myProperties.push_back(std::string(buffer + j, 128));
		}
		if (propCur < 0 || (std::size_t)propCur >= myBBD.size()) {
			break;
		}
		propCur = myBBD.at(propCur);
	} while (propCur >= 0 && propCur < (int)(myStreamSize / mySectorSize));
	return true;
}

bool OleStorage::readAllEntries() {
	int propCount = myProperties.size();
	for (int i = 0; i < propCount; ++i) {
		OleEntry entry;
		bool result = readOleEntry(i, entry);
		if (!result) {
			break;
		}
		if (entry.type == OleEntry::ROOT_DIR) {
			myRootEntryIndex = i;
		}
		myEntries.push_back(entry);
	}
	if (myRootEntryIndex < 0) {
		return false;
	}
	return true;
}

bool OleStorage::readOleEntry(int propNumber, OleEntry &e) {
	static const std::string ROOT_ENTRY = "Root Entry";

	std::string property = myProperties.at(propNumber);

	char oleType = property.at(0x42); //offset for Ole Type
	if (oleType != 1 && oleType != 2 && oleType != 3 && oleType != 5) {
		ZLLogger::Instance().println("DocPlugin", "entry -- not right ole type");
		return false;
	}

	e.type = (OleEntry::Type)oleType;

	int nameLength = OleUtil::getU2Bytes(property.c_str(), 0x40); //offset for value entry's name length
	e.name.clear();
	e.name.reserve(33); //max size of entry name

	if ((unsigned int)nameLength >= property.size()) {
		return false;
	}
	for (int i = 0; i < nameLength; i+=2) {
		char c = property.at(i);
		if (c != 0) {
			e.name += c;
		}
	}

	e.length = OleUtil::getU4Bytes(property.c_str(), 0x78); //offset for entry's length value
	e.isBigBlock = e.length >= 0x1000 || e.name == ROOT_ENTRY;

	// Read sector chain
	if (property.size() < 0x74 + 4) {
		ZLLogger::Instance().println("DocPlugin", "problems with reading ole entry");
		return false;
	}
	int chainCur = OleUtil::get4Bytes(property.c_str(), 0x74); //offset for start block of entry
	if (chainCur >= 0 && (chainCur <= (int)(myStreamSize / (e.isBigBlock ? mySectorSize : myShortSectorSize)))) {
		//filling blocks with chains
		do {
			e.blocks.push_back((unsigned int)chainCur);
			if (e.isBigBlock && (std::size_t)chainCur < myBBD.size()) {
				chainCur = myBBD.at(chainCur);
			} else if (!mySBD.empty() && (std::size_t)chainCur < mySBD.size()) {
				chainCur = mySBD.at(chainCur);
			} else {
				chainCur = -1;
			}
		} while (chainCur > 0 &&
						chainCur < (int)(e.isBigBlock ? myBBD.size() : mySBD.size()) &&
						e.blocks.size() <= e.length / (e.isBigBlock ? mySectorSize : myShortSectorSize));
	}
	e.length = std::min(e.length, (unsigned int)((e.isBigBlock ? mySectorSize : myShortSectorSize) * e.blocks.size()));
	return true;
}

bool OleStorage::countFileOffsetOfBlock(const OleEntry &e, unsigned int blockNumber, unsigned int &result) const {
	//TODO maybe better syntax can be used?
	if (e.blocks.size() <= (std::size_t)blockNumber) {
		ZLLogger::Instance().println("DocPlugin", "countFileOffsetOfBlock can't be done, blockNumber is invalid");
		return false;
	}
	if (e.isBigBlock) {
		result = BBD_BLOCK_SIZE + e.blocks.at(blockNumber) * mySectorSize;
	} else {
		unsigned int sbdPerSector = mySectorSize / myShortSectorSize;
		unsigned int sbdSectorNumber = e.blocks.at(blockNumber) / sbdPerSector;
		unsigned int sbdSectorMod = e.blocks.at(blockNumber) % sbdPerSector;
		if (myEntries.at(myRootEntryIndex).blocks.size() <= (std::size_t)sbdSectorNumber) {
			ZLLogger::Instance().println("DocPlugin", "countFileOffsetOfBlock can't be done, invalid sbd data");
			return false;
		}
		result = BBD_BLOCK_SIZE + myEntries.at(myRootEntryIndex).blocks.at(sbdSectorNumber) * mySectorSize + sbdSectorMod * myShortSectorSize;
	}
	return true;
}

bool OleStorage::getEntryByName(std::string name, OleEntry &returnEntry) const {
	//TODO fix the workaround for duplicates streams: now it takes a stream with max length
	unsigned int maxLength = 0;
	for (std::size_t i = 0; i < myEntries.size(); ++i) {
		const OleEntry &entry = myEntries.at(i);
		if (entry.name == name && entry.length >= maxLength) {
			returnEntry = entry;
			maxLength = entry.length;
		}
	}
	return maxLength > 0;
}


