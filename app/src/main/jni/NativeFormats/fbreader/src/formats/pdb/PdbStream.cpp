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

#include <cstring>

#include <ZLFile.h>

#include "PdbStream.h"

PdbStream::PdbStream(const ZLFile &file) : myBase(file.inputStream()) {
	myBuffer = 0;
}

PdbStream::~PdbStream() {
}

bool PdbStream::open() {
	close();
	if (myBase.isNull() || !myBase->open() || !myHeader.read(myBase)) {
		return false;
	}
	// myBase offset: startOffset + 78 + 8 * records number ( myHeader.Offsets.size() )
	
	myBase->seek(myHeader.Offsets[0], true);
	// myBase offset: Offset[0] - zero record
	
	myBufferLength = 0;
	myBufferOffset = 0;

	myOffset = 0;

	return true;
}

size_t PdbStream::read(char *buffer, size_t maxSize) {
	maxSize = std::min(maxSize, (size_t)std::max((int)sizeOfOpened() - (int)offset(), 0));
	size_t realSize = 0;
	while (realSize < maxSize) {
		if (!fillBuffer()) {
			break;
		}
		size_t size = std::min((size_t)(maxSize - realSize), (size_t)(myBufferLength - myBufferOffset));
		
		if (size > 0) {
			if (buffer != 0) {
				memcpy(buffer + realSize, myBuffer + myBufferOffset, size);
			}
			realSize += size;
			myBufferOffset += size;
		}
	}
			
	myOffset += realSize;
	return realSize;
}

void PdbStream::close() {
	if (!myBase.isNull()) {
		myBase->close();
	}
	if (myBuffer != 0) {
		delete[] myBuffer;
		myBuffer = 0;
	}
}

void PdbStream::seek(int offset, bool absoluteOffset) {
	if (absoluteOffset) {
		offset -= this->offset();
	}
	if (offset > 0) {
		read(0, offset);
	} else if (offset < 0) {
		offset += this->offset();
		open();
		if (offset >= 0) {
			read(0, offset);
		}
	}
}

size_t PdbStream::offset() const {
	return myOffset;
}

size_t PdbStream::recordOffset(size_t index) const {
	return index < myHeader.Offsets.size() ? 
		myHeader.Offsets[index] : myBase->sizeOfOpened();
}
