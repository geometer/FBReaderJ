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

#include <cstdint>
#include <cstring>
#include <algorithm>

#include <AndroidUtil.h>

#include <ZLFile.h>
#include <ZLDir.h>
#include <ZLOutputStream.h>
#include <ZLStringUtil.h>

#include "ZLCachedMemoryAllocator.h"

ZLCachedMemoryAllocator::ZLCachedMemoryAllocator(const std::size_t rowSize,
		const std::string &directoryName, const std::string &fileExtension) :
	myRowSize(rowSize),
	myCurrentRowSize(0),
	myOffset(0),
	myHasChanges(false),
	myFailed(false),
	myDirectoryName(directoryName),
	myFileExtension(fileExtension) {
	ZLFile(directoryName).directory(true);
}

ZLCachedMemoryAllocator::~ZLCachedMemoryAllocator() {
	flush();
	for (std::vector<char*>::const_iterator it = myPool.begin(); it != myPool.end(); ++it) {
		delete[] *it;
	}
}

void ZLCachedMemoryAllocator::flush() {
	if (!myHasChanges) {
		return;
	}
	char *ptr = myPool.back() + myOffset;
	*ptr++ = 0;
	*ptr = 0;
	writeCache(myOffset + 2);
	myHasChanges = false;
}

std::string ZLCachedMemoryAllocator::makeFileName(std::size_t index) {
	std::string name(myDirectoryName);
	name.append("/");
	ZLStringUtil::appendNumber(name, index);
	return name.append(".").append(myFileExtension);
}

void ZLCachedMemoryAllocator::writeCache(std::size_t blockLength) {
	if (myFailed || myPool.size() == 0) {
		return;
	}
	const std::size_t index = myPool.size() - 1;
	const std::string fileName = makeFileName(index);
	ZLFile file(fileName);
	shared_ptr<ZLOutputStream> stream = file.outputStream();
	if (stream.isNull() || !stream->open()) {
		myFailed = true;
		return;
	}
	stream->write(myPool[index], blockLength);
	stream->close();
}

char *ZLCachedMemoryAllocator::allocate(std::size_t size) {
	myHasChanges = true;
	if (myPool.empty()) {
		myCurrentRowSize = std::max(myRowSize, size + 2 + sizeof(char*));
		myPool.push_back(new char[myCurrentRowSize]);
	} else if (myOffset + size + 2 + sizeof(char*) > myCurrentRowSize) {
		myCurrentRowSize = std::max(myRowSize, size + 2 + sizeof(char*));
		char *row = new char[myCurrentRowSize];

		char *ptr = myPool.back() + myOffset;
		*ptr++ = 0;
		*ptr++ = 0;
		std::memcpy(ptr, &row, sizeof(char*));
		writeCache(myOffset + 2);

		myPool.push_back(row);
		myOffset = 0;
	}
	char *ptr = myPool.back() + myOffset;
	myOffset += size;
	return ptr;
}

char *ZLCachedMemoryAllocator::reallocateLast(char *ptr, std::size_t newSize) {
	myHasChanges = true;
	const std::size_t oldOffset = ptr - myPool.back();
	if (oldOffset + newSize + 2 + sizeof(char*) <= myCurrentRowSize) {
		myOffset = oldOffset + newSize;
		return ptr;
	} else {
		myCurrentRowSize = std::max(myRowSize, newSize + 2 + sizeof(char*));
		char *row = new char[myCurrentRowSize];
		std::memcpy(row, ptr, myOffset - oldOffset);

		*ptr++ = 0;
		*ptr++ = 0;
		std::memcpy(ptr, &row, sizeof(char*));
		writeCache(oldOffset + 2);

		myPool.push_back(row);
		myOffset = newSize;
		return row;
	}
}
