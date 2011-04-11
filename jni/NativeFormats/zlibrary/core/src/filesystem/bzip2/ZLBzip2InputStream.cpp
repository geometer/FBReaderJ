/*
 * Copyright (C) 2004-2010 Geometer Plus <contact@geometerplus.com>
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

#include "ZLBzip2InputStream.h"

ZLBzip2InputStream::ZLBzip2InputStream(shared_ptr<ZLInputStream> base) : myBaseStream(new ZLInputStreamDecorator(base)), myBaseBuffer(0) {
	myBzStream.bzalloc = 0;
	myBzStream.bzfree = 0;
	myBzStream.opaque = 0;
}

ZLBzip2InputStream::~ZLBzip2InputStream() {
	close();
}

const size_t BUFFER_SIZE = 2048;

bool ZLBzip2InputStream::open() {
	close();

	if (BZ2_bzDecompressInit(&myBzStream, 0, 0) != BZ_OK) {
		return false;
	}

	if (!myBaseStream->open()) {
		return false;
	}
	myBaseAvailableSize = myBaseStream->sizeOfOpened();
	myBzStream.avail_in = 0;
	myBzStream.total_in_lo32 = myBaseAvailableSize;
	//myBzStream.total_in_hi32 = myBaseAvailableSize >> 32;
	myBzStream.total_in_hi32 = 0;
	myBaseBuffer = new char[BUFFER_SIZE];
	myTrashBuffer = new char[BUFFER_SIZE];
	myOffset = 0;
	
	return true;
}

size_t ZLBzip2InputStream::read(char *buffer, size_t maxSize) {
	myBzStream.avail_out = maxSize;
	myBzStream.next_out = buffer;

	while (((myBaseAvailableSize > 0) || (myBzStream.avail_in > 0)) && (myBzStream.avail_out > 0)) {
		if (myBzStream.avail_in == 0) {
			myBzStream.avail_in = std::min(BUFFER_SIZE, myBaseAvailableSize);
			myBzStream.next_in = myBaseBuffer;
			myBaseStream->read(myBaseBuffer, myBzStream.avail_in);
			myBaseAvailableSize -= myBzStream.avail_in;
		}
		if (BZ2_bzDecompress(&myBzStream) != BZ_OK) {
			myBaseAvailableSize = 0;
			myBzStream.avail_in = 0;
			break;
		}
	}
	size_t realSize = maxSize - myBzStream.avail_out;
	myOffset += realSize;
	return realSize;
}

void ZLBzip2InputStream::close() {
	myBaseStream->close();
	if (myBaseBuffer != 0) {
		delete[] myBaseBuffer;
		delete[] myTrashBuffer;
		myBaseBuffer = 0;
		myTrashBuffer = 0;
		BZ2_bzDecompressEnd(&myBzStream);
	}
}

void ZLBzip2InputStream::seek(int offset, bool absoluteOffset) {
	if (absoluteOffset) {
		offset -= this->offset();
	}
	if (offset < 0) {
		offset += this->offset();
		open();
	}
	if (offset > 0) {
		while (offset != 0) {
			size_t rSize = read(myTrashBuffer, std::min(BUFFER_SIZE, (size_t)offset));
			if (rSize == 0) {
				break;
			}
			offset -= std::min(rSize, (size_t)offset);
		}
	}
}

size_t ZLBzip2InputStream::offset() const {
	return myOffset;
}

size_t ZLBzip2InputStream::sizeOfOpened() {
	// TODO: implement
	return 0;
}
