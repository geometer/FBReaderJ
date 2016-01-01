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

#include <ZLFile.h>

#include "FontMap.h"

FileInfo::FileInfo(const std::string &path, shared_ptr<FileEncryptionInfo> info) : Path(path), EncryptionInfo(info) {
}

void FontEntry::addFile(bool bold, bool italic, const std::string &filePath, shared_ptr<FileEncryptionInfo> encryptionInfo) {
	shared_ptr<FileInfo> fileInfo = new FileInfo(filePath, encryptionInfo);
	if (bold) {
		if (italic) {
			BoldItalic = fileInfo;
		} else {
			Bold = fileInfo;
		}
	} else {
		if (italic) {
			Italic = fileInfo;
		} else {
			Normal = fileInfo;
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

static bool compareFileInfos(shared_ptr<FileInfo> info0, shared_ptr<FileInfo> info1) {
	return info0.isNull() ? info1.isNull() : (!info1.isNull() && info0->Path == info1->Path);
}

bool FontEntry::operator == (const FontEntry &other) const {
	return
		compareFileInfos(Normal, other.Normal) &&
		compareFileInfos(Bold, other.Bold) &&
		compareFileInfos(Italic, other.Italic) &&
		compareFileInfos(BoldItalic, other.BoldItalic);
}

bool FontEntry::operator != (const FontEntry &other) const {
	return !operator ==(other);
}

void FontMap::append(const std::string &family, bool bold, bool italic, const std::string &path, shared_ptr<FileEncryptionInfo> encryptionInfo) {
	const ZLFile fontFile(path);
	shared_ptr<FontEntry> entry = myMap[family];
	if (entry.isNull()) {
		entry = new FontEntry();
		myMap[family] = entry;
	}
	entry->addFile(bold, italic, fontFile.path(), encryptionInfo);
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
