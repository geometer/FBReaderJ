/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#include "ZLInputStream.h"

ZLInputStreamDecorator::ZLInputStreamDecorator(shared_ptr<ZLInputStream> decoratee) : myBaseStream(decoratee), myBaseOffset(0) {
}

bool ZLInputStreamDecorator::open() {
	bool result = myBaseStream->open();
	myBaseOffset = myBaseStream->offset();
	return result;
}

std::size_t ZLInputStreamDecorator::read(char *buffer, std::size_t maxSize) {
	myBaseStream->seek(myBaseOffset, true);
	std::size_t result = myBaseStream->read(buffer, maxSize);
	myBaseOffset = myBaseStream->offset();
	return result;
}

void ZLInputStreamDecorator::close() {
	myBaseStream->close();
}

void ZLInputStreamDecorator::seek(int offset, bool absoluteOffset) {
	if (absoluteOffset) {
		myBaseStream->seek(offset, true);
	} else {
		myBaseStream->seek(myBaseOffset + offset, true);
	}
	myBaseOffset = myBaseStream->offset();
}

std::size_t ZLInputStreamDecorator::offset() const {
	return myBaseOffset;
}

std::size_t ZLInputStreamDecorator::sizeOfOpened() {
	return myBaseStream->sizeOfOpened();
}
