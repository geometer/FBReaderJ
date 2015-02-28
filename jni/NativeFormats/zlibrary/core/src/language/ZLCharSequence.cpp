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

#include <string>

#include "ZLCharSequence.h"

ZLCharSequence::ZLCharSequence(const char *ptr, std::size_t size) : mySize(size) {
	if (mySize == 0) {
		myHead = 0;
		return;
	}
	myHead = new char[mySize];
	for (std::size_t count = 0; count < mySize; ++count){
		myHead[count] = ptr[count];
	}
}

ZLCharSequence::ZLCharSequence(const ZLCharSequence& other) : mySize(other.mySize) {
	if (mySize == 0) {
		myHead = 0;
		return;
	}
	myHead = new char[other.mySize];
	for (std::size_t count = 0; count < mySize; ++count) {
		myHead[count] = other.myHead[count];
	}
}

ZLCharSequence::ZLCharSequence(const std::string &hexSequence) {
	mySize = (hexSequence.size() + 1) / 5;
	myHead = new char[mySize];
	for (std::size_t count = 0; count < mySize; ++count){
		char a = hexSequence[count * 5 + 2];
		char b = hexSequence[count * 5 + 3];
		a -= (a >= 97) ? 87 : 48;
		b -= (b >= 97) ? 87 : 48;
		myHead[count] = a*16 + b;
	}
}

ZLCharSequence& ZLCharSequence::operator= (const ZLCharSequence& other) {
	if (this != &other) {
		if (mySize != other.mySize && myHead != 0) {
			delete[] myHead;
			myHead = 0;
		}
		mySize = other.mySize;
		if (other.myHead != 0) {
			if (myHead == 0) {
				myHead = new char[mySize];
			}
			for (std::size_t count = 0; count < mySize; ++count) {
				myHead[count] = other.myHead[count];
			}
		}
	}
	return *this;
}

std::string ZLCharSequence::toHexSequence() const {
	std::string result;
	static const char table[] = "0123456789abcdef";
	for (std::size_t count = 0;; ++count) {
		result += "0x";
		result += table[(myHead[count] >> 4) & 0x0F];
		result += table[myHead[count] & 0x0F];
		if (count == mySize-1) {
			return result;
		}
		result += " ";
	}
	return result;
}

int ZLCharSequence::compareTo(const ZLCharSequence &other) const {
	int difference = mySize - other.mySize;
	if (difference != 0) {
		return difference;
	}
	for (std::size_t i = 0; i < mySize; ++i) {
		int a = (int)(unsigned int)(unsigned char) myHead[i];
		int b = (int)(unsigned int)(unsigned char) other.myHead[i];
		difference = a - b;
		if (difference != 0) {
			return difference;
		}
	}
	return 0;
}
