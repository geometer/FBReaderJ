/*
 * Copyright (C) 2004-2011 Geometer Plus <contact@geometerplus.com>
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

#include <string.h>

#include <algorithm>

#include "ZLTextRowMemoryAllocator.h"

ZLTextRowMemoryAllocator::ZLTextRowMemoryAllocator(const size_t rowSize) : myRowSize(rowSize), myOffset(0) {
}

ZLTextRowMemoryAllocator::~ZLTextRowMemoryAllocator() {
	for (std::vector<char*>::const_iterator it = myPool.begin(); it != myPool.end(); ++it) {
		delete[] *it;
	}
}

char *ZLTextRowMemoryAllocator::allocate(size_t size) {
	if (myPool.empty()) {
		myCurrentRowSize = std::max(myRowSize, size + 1 + sizeof(char*));
		myPool.push_back(new char[myCurrentRowSize]);
	} else if (myOffset + size + 1 + sizeof(char*) > myRowSize) {
		myCurrentRowSize = std::max(myRowSize, size + 1 + sizeof(char*));
		char *row = new char[myCurrentRowSize];
		*(myPool.back() + myOffset) = 0;
		memcpy(myPool.back() + myOffset + 1, &row, sizeof(char*));
		myPool.push_back(row);
		myOffset = 0;
	}
	char *ptr = myPool.back() + myOffset;
	myOffset += size;
	return ptr;
}

char *ZLTextRowMemoryAllocator::reallocateLast(char *ptr, size_t newSize) {
	if (ptr + newSize + 1 + sizeof(char*) <= myPool.back() + myCurrentRowSize) {
		myOffset = ptr - myPool.back() + newSize;
		return ptr;
	} else {
		myCurrentRowSize = std::max(myRowSize, newSize + 1 + sizeof(char*));
		char *row = new char[myCurrentRowSize];
		memcpy(row, ptr, myOffset - (ptr - myPool.back()));
		*ptr = 0;
		memcpy(ptr + 1, &row, sizeof(char*));
		myPool.push_back(row);
		myOffset = newSize;
		return row;
	}
}
