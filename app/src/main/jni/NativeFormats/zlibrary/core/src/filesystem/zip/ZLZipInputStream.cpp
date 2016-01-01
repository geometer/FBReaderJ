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

#include <algorithm>

#include "ZLZip.h"
#include "ZLZipHeader.h"
#include "ZLZDecompressor.h"
#include "../ZLFSManager.h"

ZLZipInputStream::ZLZipInputStream(shared_ptr<ZLInputStream> base, const std::string &baseName, const std::string &entryName) : myBaseStream(new ZLInputStreamDecorator(base)), myBaseName(baseName), myEntryName(entryName), myIsOpen(false), myUncompressedSize(0) {
}

ZLZipInputStream::~ZLZipInputStream() {
	close();
}

bool ZLZipInputStream::open() {
	close();

	ZLZipEntryCache::Info info = ZLZipEntryCache::cache(myBaseName, *myBaseStream)->info(myEntryName);

	if (!myBaseStream->open()) {
		return false;
	}

	if (info.Offset == -1) {
		close();
		return false;
	}
	myBaseStream->seek(info.Offset, true);

	if (info.CompressionMethod == 0) {
		myIsDeflated = false;
	} else if (info.CompressionMethod == 8) {
		myIsDeflated = true;
	} else {
		close();
		return false;
	}
	myUncompressedSize = info.UncompressedSize;
	myAvailableSize = info.CompressedSize;
	if (myAvailableSize == 0) {
		myAvailableSize = (std::size_t)-1;
	}

	if (myIsDeflated) {
		myDecompressor = new ZLZDecompressor(myAvailableSize);
	}

	myOffset = 0;
	myIsOpen = true;
	return true;
}

std::size_t ZLZipInputStream::read(char *buffer, std::size_t maxSize) {
	if (!myIsOpen) {
		return 0;
	}

	std::size_t realSize = 0;
	if (myIsDeflated) {
		realSize = myDecompressor->decompress(*myBaseStream, buffer, maxSize);
		myOffset += realSize;
	} else {
		realSize = myBaseStream->read(buffer, std::min(maxSize, myAvailableSize));
		myAvailableSize -= realSize;
		myOffset += realSize;
	}
	return realSize;
}

void ZLZipInputStream::close() {
	myIsOpen = false;
	myDecompressor = 0;
	if (!myBaseStream.isNull()) {
		myBaseStream->close();
	}
}

void ZLZipInputStream::seek(int offset, bool absoluteOffset) {
	if (absoluteOffset) {
		offset -= this->offset();
	}
	if (offset > 0) {
		read(0, offset);
	} else if (offset < 0) {
		offset += this->offset();
		if (open() && offset > 0) {
			read(0, offset);
		}
	}
}

std::size_t ZLZipInputStream::offset() const {
	return myOffset;
}

std::size_t ZLZipInputStream::sizeOfOpened() {
	return myUncompressedSize;
}
