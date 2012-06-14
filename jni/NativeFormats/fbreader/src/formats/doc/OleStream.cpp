/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

#include "OleStream.h"
#include "OleUtil.h"

OleStream::OleStream(shared_ptr<OleStorage> storage, OleEntry oleEntry, shared_ptr<ZLInputStream> stream) :
	myStorage(storage),
	myOleEntry(oleEntry),
	myBaseStream(stream) {
	myOleOffset = 0;
}


bool OleStream::open() {
	if (myOleEntry.type != OleEntry::STREAM) {
		return false;
	}
	return true;
}

size_t OleStream::read(char *buffer, size_t maxSize) {
	size_t length = maxSize;
	size_t readedBytes = 0;
	size_t bytesLeftInCurBlock;
	unsigned int newFileOffset;

	unsigned int curBlockNumber, modBlock;
	size_t toReadBlocks, toReadBytes;

	if (myOleOffset + length > myOleEntry.length) {
		length = myOleEntry.length - myOleOffset;
	}

	size_t sectorSize = (size_t)(myOleEntry.isBigBlock ? myStorage->getSectorSize() : myStorage->getShortSectorSize());

	curBlockNumber = myOleOffset / sectorSize;
	if (curBlockNumber >= myOleEntry.blocks.size()) {
		return 0;
	}
	modBlock = myOleOffset % sectorSize;
	bytesLeftInCurBlock = sectorSize - modBlock;
	if (bytesLeftInCurBlock < length) {
		toReadBlocks = (length - bytesLeftInCurBlock) / sectorSize;
		toReadBytes = (length - bytesLeftInCurBlock) % sectorSize;
	} else {
		toReadBlocks = toReadBytes = 0;
	}

	newFileOffset = myStorage->getFileOffsetOfBlock(myOleEntry, curBlockNumber) + modBlock;
	myBaseStream->seek(newFileOffset, true);

	readedBytes = myBaseStream->read(buffer, std::min(length, bytesLeftInCurBlock));
	for (size_t i = 0; i < toReadBlocks; ++i) {
		size_t readbytes;
		++curBlockNumber;
		newFileOffset = myStorage->getFileOffsetOfBlock(myOleEntry, curBlockNumber);
		myBaseStream->seek(newFileOffset, true);
		readbytes = myBaseStream->read(buffer + readedBytes, std::min(length - readedBytes, sectorSize));
		readedBytes += readbytes;
	}
	if (toReadBytes > 0) {
		size_t readbytes;
		++curBlockNumber;
		newFileOffset = myStorage->getFileOffsetOfBlock(myOleEntry, curBlockNumber);
		myBaseStream->seek(newFileOffset, true);
		readbytes = myBaseStream->read(buffer + readedBytes, toReadBytes);
		readedBytes += readbytes;
	}
	myOleOffset += readedBytes;
	return readedBytes;
}

bool OleStream::eof() const {
	return (myOleOffset >= myOleEntry.length);
}


void OleStream::close() {
}

bool OleStream::seek(unsigned int offset, bool absoluteOffset) {
	unsigned int newOleOffset = 0;
	unsigned int newFileOffset;

	if (absoluteOffset) {
		newOleOffset = offset;
	} else {
		newOleOffset = myOleOffset + offset;
	}

	newOleOffset = std::min(newOleOffset, myOleEntry.length);

	unsigned int sectorSize = (myOleEntry.isBigBlock ? myStorage->getSectorSize() : myStorage->getShortSectorSize());
	unsigned int blockNumber = newOleOffset / sectorSize;
	if (blockNumber >= myOleEntry.blocks.size()) {
		return false;
	}

	unsigned int modBlock = newOleOffset % sectorSize;
	newFileOffset = myStorage->getFileOffsetOfBlock(myOleEntry, blockNumber) + modBlock;
	myBaseStream->seek(newFileOffset, true);
	myOleOffset = newOleOffset;
	return true;
}

size_t OleStream::offset() {
	return myOleOffset;
}
