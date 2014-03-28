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

#include <ZLLogger.h>
#include <ZLStringUtil.h>
#include <ZLFile.h>

#include "FontMap.h"

void FontEntry::addFile(const std::string &weight, const std::string &style, const std::string &filePath) {
	if (weight == "bold") {
		if (style == "italic") {
			BoldItalic = new std::string(filePath);
		} else {
			Bold = new std::string(filePath);
		}
	} else {
		if (style == "italic") {
			Italic = new std::string(filePath);
		} else {
			Normal = new std::string(filePath);
		}
	}
}

static bool compareStringPtrs(shared_ptr<std::string> str0, shared_ptr<std::string> str1) {
	return str0.isNull() ? str1.isNull() : (!str1.isNull() && *str0 == *str1);
}

bool FontEntry::operator == (const FontEntry &other) const {
	return
		compareStringPtrs(Normal, other.Normal) &&
		compareStringPtrs(Bold, other.Bold) &&
		compareStringPtrs(Italic, other.Italic) &&
		compareStringPtrs(BoldItalic, other.BoldItalic);
}

bool FontEntry::operator != (const FontEntry &other) const {
	return !operator ==(other);
}

bool FontMap::operator == (const FontMap &other) const {
	return myMap == other.myMap;
}

bool FontMap::operator != (const FontMap &other) const {
	return !operator ==(other);
}

void FontMap::appendFontFace(const std::string &family, const std::string &weight, const std::string &style, const std::string &path) {
	const ZLFile fontFile(path);
	myMap[family].addFile(weight, style, fontFile.path());
	ZLLogger::Instance().println("FONT", family + " => " + fontFile.path());
}
