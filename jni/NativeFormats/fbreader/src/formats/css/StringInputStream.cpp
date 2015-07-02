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

#include <ZLLogger.h>

#include "StringInputStream.h"

StringInputStream::StringInputStream(const char *cString, std::size_t len) : myCString(cString), myLength(len), myOffset(0) {
	//ZLLogger::Instance().registerClass("StringInputStream");
}

bool StringInputStream::open() {
	return true;
}

std::size_t StringInputStream::read(char *buffer, std::size_t maxSize) {
	const std::size_t len = std::min(maxSize, myLength - myOffset);
	std::memcpy(buffer, myCString + myOffset, len);
	myOffset += len;
	//ZLLogger::Instance().println("StringInputStream", std::string(buffer, len));
	return len;
}

void StringInputStream::close() {
}

void StringInputStream::seek(int offset, bool absoluteOffset) {
	if (!absoluteOffset) {
		offset += myOffset;
	}
	myOffset = std::max(0, std::min(offset, (int)myLength));
}

std::size_t StringInputStream::offset() const {
	return myOffset;
}

std::size_t StringInputStream::sizeOfOpened() {
	return myLength;
}
