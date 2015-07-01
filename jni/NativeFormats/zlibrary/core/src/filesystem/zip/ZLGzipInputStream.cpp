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

#include "ZLZip.h"
#include "ZLZDecompressor.h"

ZLGzipInputStream::ZLGzipInputStream(shared_ptr<ZLInputStream> stream) : myBaseStream(new ZLInputStreamDecorator(stream)), myFileSize(0) {
}

ZLGzipInputStream::~ZLGzipInputStream() {
	close();
}

bool ZLGzipInputStream::open() {
	close();

	if (!myBaseStream->open()) {
		return false;
	}

	myFileSize = myBaseStream->sizeOfOpened();

	unsigned char id1;
	unsigned char id2;
	unsigned char cm;

	myBaseStream->read((char*)&id1, 1);
	myBaseStream->read((char*)&id2, 1);
	myBaseStream->read((char*)&cm, 1);
	if ((id1 != 31) || (id2 != 139) || (cm != 8)) {
		myBaseStream->close();
		return false;
	}

	//const unsigned char FTEXT = 1 << 0;
	const unsigned char FHCRC = 1 << 1;
	const unsigned char FEXTRA = 1 << 2;
	const unsigned char FNAME = 1 << 3;
	const unsigned char FCOMMENT = 1 << 4;
	unsigned char flg;
	myBaseStream->read((char*)&flg, 1);
	myBaseStream->seek(6, false);
	if (flg & FEXTRA) {
		unsigned char b0, b1;
		myBaseStream->read((char*)&b0, 1);
		myBaseStream->read((char*)&b1, 1);
		unsigned short xlen = (((unsigned short)b1) << 8) + b0;
		myBaseStream->seek(xlen, false);
	}
	if (flg & FNAME) {
		unsigned char b;
		do {
			myBaseStream->read((char*)&b, 1);
		} while (b != 0);
	}
	if (flg & FCOMMENT) {
		unsigned char b;
		do {
			myBaseStream->read((char*)&b, 1);
		} while (b != 0);
	}
	if (flg & FHCRC) {
		myBaseStream->seek(2, false);
	}

	myDecompressor = new ZLZDecompressor(myFileSize - myBaseStream->offset() - 8);
	myOffset = 0;

	return true;
}

std::size_t ZLGzipInputStream::read(char *buffer, std::size_t maxSize) {
	std::size_t realSize = myDecompressor->decompress(*myBaseStream, buffer, maxSize);
	myOffset += realSize;
	return realSize;
}

void ZLGzipInputStream::close() {
	myDecompressor = 0;
	myBaseStream->close();
}

void ZLGzipInputStream::seek(int offset, bool absoluteOffset) {
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

std::size_t ZLGzipInputStream::offset() const {
	return myOffset;
}

std::size_t ZLGzipInputStream::sizeOfOpened() {
	// TODO: implement
	return 0;
}
