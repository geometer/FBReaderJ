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
#include <string>

#include "BitReader.h"

BitReader::BitReader(const unsigned char* data, size_t size) : myOffset(0), myLength(size * 8) {
	myData = new unsigned char[size + 4];
	memcpy(myData, data, size);
	memset(myData + size, 0x00, 4);
}

BitReader::~BitReader() {
	delete[] myData;
}

unsigned long long BitReader::peek(size_t n) {
	if (n > 32) {
		return 0;
	}
	unsigned long long r = 0;
	size_t g = 0;
	while (g < n) {
		r = (r << 8) | myData[(myOffset + g) >> 3]; 
		g = g + 8 - ((myOffset + g) & 7);
	}
	unsigned long long mask = 1;
	mask = (mask << n) - 1;
	return (r >> (g - n)) & mask;
}

bool BitReader::eat(size_t n) {
	myOffset += n;
	return myOffset <= myLength;
}

size_t BitReader::left() const {
	return myLength - myOffset;
}
