/*
 * Copyright (C) 2004-2014 Geometer Plus <contact@geometerplus.com>
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

#include "DocAnsiConverter.h"

DocAnsiConverter::DocAnsiConverter() {
	myCharMap[0x82] = "\u201A";
	myCharMap[0x83] = "\u0192";
	myCharMap[0x84] = "\u201E";
	myCharMap[0x85] = "\u2026";
	myCharMap[0x86] = "\u2020";
	myCharMap[0x87] = "\u2021";
	myCharMap[0x88] = "\u02C6";
	myCharMap[0x89] = "\u2030";
	myCharMap[0x8A] = "\u0160";
	myCharMap[0x8B] = "\u2039";
	myCharMap[0x8C] = "\u0152";
	myCharMap[0x91] = "\u2018";
	myCharMap[0x92] = "\u2019";
	myCharMap[0x93] = "\u201C";
	myCharMap[0x94] = "\u201D";
	myCharMap[0x95] = "\u2022";
	myCharMap[0x96] = "\u2013";
	myCharMap[0x97] = "\u2014";
	myCharMap[0x98] = "\u02DC";
	myCharMap[0x99] = "\u2122";
	myCharMap[0x9A] = "\u0161";
	myCharMap[0x9B] = "\u203A";
	myCharMap[0x9C] = "\u0153";
	myCharMap[0x9F] = "\u0178";
}

void DocAnsiConverter::convert(std::string &dst, const char *srcStart, const char *srcEnd) {
	for (const char *ptr = srcStart; ptr < srcEnd; ++ptr) {
		if ((*ptr & 0x80) == 0) {
			dst.append(1, *ptr);
		} else if (myCharMap.find((int)(*ptr)) != myCharMap.end()) {
			dst.append(myCharMap[(int)(*ptr)]);
		}
	}
}


