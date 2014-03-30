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

void FontEntry::addFile(bool bold, bool italic, const std::string &filePath) {
	if (bold) {
		if (italic) {
			BoldItalic = new std::string(filePath);
		} else {
			Bold = new std::string(filePath);
		}
	} else {
		if (italic) {
			Italic = new std::string(filePath);
		} else {
			Normal = new std::string(filePath);
		}
	}
}

void FontEntry::merge(const FontEntry &fontEntry) {
	if (!fontEntry.Normal.isNull()) {
		Normal = fontEntry.Normal;
	}
	if (!fontEntry.Bold.isNull()) {
		Bold = fontEntry.Bold;
	}
	if (!fontEntry.Italic.isNull()) {
		Italic = fontEntry.Italic;
	}
	if (!fontEntry.BoldItalic.isNull()) {
		BoldItalic = fontEntry.BoldItalic;
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

void FontMap::append(const std::string &family, bool bold, bool italic, const std::string &path) {
	const ZLFile fontFile(path);
	shared_ptr<FontEntry> entry = myMap[family];
	if (entry.isNull()) {
		entry = new FontEntry();
		myMap[family] = entry;
	}
	entry->addFile(bold, italic, fontFile.path());
}

void FontMap::merge(const FontMap &fontMap) {
	for (std::map<std::string,shared_ptr<FontEntry> >::const_iterator it = fontMap.myMap.begin(); it != fontMap.myMap.end(); ++it) {
		if (!it->second.isNull()) {
			shared_ptr<FontEntry> entry = myMap[it->first];
			if (entry.isNull()) {
				entry = new FontEntry();
				myMap[it->first] = entry;
			}
			entry->merge(*it->second);
		}
	}
}

shared_ptr<FontEntry> FontMap::get(const std::string &family) {
	return myMap[family];
}

std::string FontMap::put(const std::string &family, shared_ptr<FontEntry> entry) {
	shared_ptr<FontEntry> existing = myMap[family];
	if (existing.isNull() || *existing == *entry) {
		myMap[family] = entry;
		return family;
	}

	for (std::map<std::string,shared_ptr<FontEntry> >::const_iterator it = myMap.begin(); it != myMap.end(); ++it) {
		if (*it->second == *entry) {
			return it->first;
		}
	}

	for (int i = 1; i < 1000; ++i) {
		std::string indexed = family + "#";
		ZLStringUtil::appendNumber(indexed, i);
		if (myMap[indexed].isNull()) {
			myMap[indexed] = entry;
			return indexed;
		}
	}

	return std::string();
}
