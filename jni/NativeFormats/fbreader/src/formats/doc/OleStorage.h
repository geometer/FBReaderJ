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

#ifndef __OLESTORAGE_H__
#define __OLESTORAGE_H__

#include <algorithm>
#include <vector>
#include <string>

#include <ZLInputStream.h>

struct OleEntry {
	enum Type {
		DIR = 1,
		STREAM = 2,
		ROOT_DIR = 5,
		LOCK_BYTES =3
	};

	typedef std::vector<unsigned int> Blocks;

	std::string name;
	unsigned int length;
	Type type;
	Blocks blocks;
	bool isBigBlock;
};

class OleStorage {

public:
	static const std::size_t BBD_BLOCK_SIZE;

public:
	OleStorage();
	bool init(shared_ptr<ZLInputStream>, std::size_t streamSize);
	void clear();
	const std::vector<OleEntry> &getEntries() const;
	bool getEntryByName(std::string name, OleEntry &entry) const;

	unsigned int getSectorSize() const;
	unsigned int getShortSectorSize() const;

public: //TODO make private
	bool countFileOffsetOfBlock(const OleEntry &e, unsigned int blockNumber, unsigned int &result) const;

private:
	bool readDIFAT(char *oleBuf);
	bool readBBD(char *oleBuf);
	bool readSBD(char *oleBuf);
	bool readProperties(char *oleBuf);

	bool readAllEntries();
	bool readOleEntry(int propNumber, OleEntry &entry);

private:

	shared_ptr<ZLInputStream> myInputStream;
	unsigned int mySectorSize, myShortSectorSize;

	std::size_t myStreamSize;
	std::vector<int> myDIFAT; //double-indirect file allocation table
	std::vector<int> myBBD; //Big Block Depot
	std::vector<int> mySBD; //Small Block Depot
	std::vector<std::string> myProperties;
	std::vector<OleEntry> myEntries;
	int myRootEntryIndex;

};

inline const std::vector<OleEntry> &OleStorage::getEntries() const { return myEntries; }
inline unsigned int OleStorage::getSectorSize() const { return mySectorSize; }
inline unsigned int OleStorage::getShortSectorSize() const { return myShortSectorSize; }

#endif /* __OLESTORAGE_H__ */
