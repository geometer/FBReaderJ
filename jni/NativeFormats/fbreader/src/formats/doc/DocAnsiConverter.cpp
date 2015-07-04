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

DocAnsiConverter::DocAnsiConverter() : myCharMap(128) {
	myCharMap[0x02] = "\u201A";
	myCharMap[0x03] = "\u0192";
	myCharMap[0x04] = "\u201E";
	myCharMap[0x05] = "\u2026";
	myCharMap[0x06] = "\u2020";
	myCharMap[0x07] = "\u2021";
	myCharMap[0x08] = "\u02C6";
	myCharMap[0x09] = "\u2030";
	myCharMap[0x0A] = "\u0160";
	myCharMap[0x0B] = "\u2039";
	myCharMap[0x0C] = "\u0152";
	myCharMap[0x11] = "\u2018";
	myCharMap[0x12] = "\u2019";
	myCharMap[0x13] = "\u201C";
	myCharMap[0x14] = "\u201D";
	myCharMap[0x15] = "\u2022";
	myCharMap[0x16] = "\u2013";
	myCharMap[0x17] = "\u2014";
	myCharMap[0x18] = "\u02DC";
	myCharMap[0x19] = "\u2122";
	myCharMap[0x1A] = "\u0161";
	myCharMap[0x1B] = "\u203A";
	myCharMap[0x1C] = "\u0153";
	myCharMap[0x1F] = "\u0178";
}

void DocAnsiConverter::convert(std::string &dst, const char *srcStart, const char *srcEnd) {
	for (const char *ptr = srcStart; ptr < srcEnd; ++ptr) {
		if ((*ptr & 0x80) == 0) {
			dst.append(1, *ptr);
		} else {
			dst.append(myCharMap[*ptr & 0x7F]);
		}
	}
}
