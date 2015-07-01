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

std::size_t OleStream::read(char *buffer, std::size_t maxSize) {
	std::size_t length = maxSize;
	std::size_t readedBytes = 0;
	std::size_t bytesLeftInCurBlock;
	unsigned int newFileOffset;

	unsigned int curBlockNumber, modBlock;
	std::size_t toReadBlocks, toReadBytes;

	if (myOleOffset + length > myOleEntry.length) {
		length = myOleEntry.length - myOleOffset;
	}

	std::size_t sectorSize = (std::size_t)(myOleEntry.isBigBlock ? myStorage->getSectorSize() : myStorage->getShortSectorSize());

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

	if (!myStorage->countFileOffsetOfBlock(myOleEntry, curBlockNumber, newFileOffset)) {
		return 0;
	}
	newFileOffset += modBlock;

	myBaseStream->seek(newFileOffset, true);

	readedBytes = myBaseStream->read(buffer, std::min(length, bytesLeftInCurBlock));
	for (std::size_t i = 0; i < toReadBlocks; ++i) {
		if (++curBlockNumber >= myOleEntry.blocks.size()) {
			break;
		}
		if (!myStorage->countFileOffsetOfBlock(myOleEntry, curBlockNumber, newFileOffset)) {
			return readedBytes;
		}
		myBaseStream->seek(newFileOffset, true);
		readedBytes += myBaseStream->read(buffer + readedBytes, std::min(length - readedBytes, sectorSize));
	}
	if (toReadBytes > 0 && ++curBlockNumber < myOleEntry.blocks.size()) {
		if (!myStorage->countFileOffsetOfBlock(myOleEntry, curBlockNumber, newFileOffset)) {
			return readedBytes;
		}
		myBaseStream->seek(newFileOffset, true);
		readedBytes += myBaseStream->read(buffer + readedBytes, toReadBytes);
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
	if (!myStorage->countFileOffsetOfBlock(myOleEntry, blockNumber, newFileOffset)) {
		return false;
	}
	newFileOffset += modBlock;
	myBaseStream->seek(newFileOffset, true);
	myOleOffset = newOleOffset;
	return true;
}

std::size_t OleStream::offset() {
	return myOleOffset;
}

ZLFileImage::Blocks OleStream::getBlockPieceInfoList(unsigned int offset, unsigned int size) const {
	ZLFileImage::Blocks list;
	unsigned int sectorSize = (myOleEntry.isBigBlock ? myStorage->getSectorSize() : myStorage->getShortSectorSize());
	unsigned int curBlockNumber = offset / sectorSize;
	if (curBlockNumber >= myOleEntry.blocks.size()) {
		return list;
	}
	unsigned int modBlock = offset % sectorSize;
	unsigned int startFileOffset = 0;
	if (!myStorage->countFileOffsetOfBlock(myOleEntry, curBlockNumber, startFileOffset)) {
		return ZLFileImage::Blocks();
	}
	startFileOffset += modBlock;

	unsigned int bytesLeftInCurBlock = sectorSize - modBlock;
	unsigned int toReadBlocks = 0, toReadBytes = 0;
	if (bytesLeftInCurBlock < size) {
		toReadBlocks = (size - bytesLeftInCurBlock) / sectorSize;
		toReadBytes = (size - bytesLeftInCurBlock) % sectorSize;
	}

	unsigned int readedBytes = std::min(size, bytesLeftInCurBlock);
	list.push_back(ZLFileImage::Block(startFileOffset, readedBytes));

	for (unsigned int i = 0; i < toReadBlocks; ++i) {
		if (++curBlockNumber >= myOleEntry.blocks.size()) {
			break;
		}
		unsigned int newFileOffset = 0;
		if (!myStorage->countFileOffsetOfBlock(myOleEntry, curBlockNumber, newFileOffset)) {
			return ZLFileImage::Blocks();
		}
		unsigned int readbytes = std::min(size - readedBytes, sectorSize);
		list.push_back(ZLFileImage::Block(newFileOffset, readbytes));
		readedBytes += readbytes;
	}
	if (toReadBytes > 0 && ++curBlockNumber < myOleEntry.blocks.size()) {
		unsigned int newFileOffset = 0;
		if (!myStorage->countFileOffsetOfBlock(myOleEntry, curBlockNumber, newFileOffset)) {
			return ZLFileImage::Blocks();
		}
		unsigned int readbytes = toReadBytes;
		list.push_back(ZLFileImage::Block(newFileOffset, readbytes));
		readedBytes += readbytes;
	}

	return concatBlocks(list);
}

ZLFileImage::Blocks OleStream::concatBlocks(const ZLFileImage::Blocks &blocks) {
	if (blocks.size() < 2) {
		return blocks;
	}
	ZLFileImage::Blocks optList;
	ZLFileImage::Block curBlock = blocks.at(0);
	unsigned int nextOffset = curBlock.offset + curBlock.size;
	for (std::size_t i = 1; i < blocks.size(); ++i) {
		ZLFileImage::Block b = blocks.at(i);
		if (b.offset == nextOffset) {
			curBlock.size += b.size;
			nextOffset += b.size;
		} else {
			optList.push_back(curBlock);
			curBlock = b;
			nextOffset = curBlock.offset + curBlock.size;
		}
	}
	optList.push_back(curBlock);
	return optList;
}

std::size_t OleStream::fileOffset() {
	//TODO maybe remove this method, it doesn't use at this time
	std::size_t sectorSize = (std::size_t)(myOleEntry.isBigBlock ? myStorage->getSectorSize() : myStorage->getShortSectorSize());
	unsigned int curBlockNumber = myOleOffset / sectorSize;
	if (curBlockNumber >= myOleEntry.blocks.size()) {
		return 0;
	}
	unsigned int modBlock = myOleOffset % sectorSize;
	unsigned int curOffset = 0;
	if (!myStorage->countFileOffsetOfBlock(myOleEntry, curBlockNumber, curOffset)) {
		return 0; //TODO maybe remove -1?
	}
	return curOffset + modBlock;
}
