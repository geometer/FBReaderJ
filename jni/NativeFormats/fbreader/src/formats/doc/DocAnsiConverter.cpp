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

DocAnsiConverter::DocAnsiConverter() : myCharMap(30) {
	myCharMap[0x82-0x82] = "\u201A";
	myCharMap[0x83-0x82] = "\u0192";
	myCharMap[0x84-0x82] = "\u201E";
	myCharMap[0x85-0x82] = "\u2026";
	myCharMap[0x86-0x82] = "\u2020";
	myCharMap[0x87-0x82] = "\u2021";
	myCharMap[0x88-0x82] = "\u02C6";
	myCharMap[0x89-0x82] = "\u2030";
	myCharMap[0x8A-0x82] = "\u0160";
	myCharMap[0x8B-0x82] = "\u2039";
	myCharMap[0x8C-0x82] = "\u0152";
	myCharMap[0x91-0x82] = "\u2018";
	myCharMap[0x92-0x82] = "\u2019";
	myCharMap[0x93-0x82] = "\u201C";
	myCharMap[0x94-0x82] = "\u201D";
	myCharMap[0x95-0x82] = "\u2022";
	myCharMap[0x96-0x82] = "\u2013";
	myCharMap[0x97-0x82] = "\u2014";
	myCharMap[0x98-0x82] = "\u02DC";
	myCharMap[0x99-0x82] = "\u2122";
	myCharMap[0x9A-0x82] = "\u0161";
	myCharMap[0x9B-0x82] = "\u203A";
	myCharMap[0x9C-0x82] = "\u0153";
	myCharMap[0x9F-0x82] = "\u0178";
}

void DocAnsiConverter::convert(std::string &dst, const char *srcStart, const char *srcEnd) {
	for (const char *ptr = srcStart; ptr < srcEnd; ++ptr) {
		if ((*ptr & 0x80) == 0) {
			dst.append(1, *ptr);
		} else if (myCharMap.at((int)(*ptr)-0x82) != "") {
			dst.append(myCharMap[(int)(*ptr)-0x82]);
		}
	}
}


