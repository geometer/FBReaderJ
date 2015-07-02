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

#ifndef __FONTMAP_H__
#define __FONTMAP_H__

#include <string>
#include <map>

#include <shared_ptr.h>

#include <FileEncryptionInfo.h>

class FileInfo {

public:
	FileInfo(const std::string &path, shared_ptr<FileEncryptionInfo> info);

public:
	const std::string Path;
	shared_ptr<FileEncryptionInfo> EncryptionInfo;
};

class FontEntry {

public:
	void addFile(bool bold, bool italic, const std::string &filePath, shared_ptr<FileEncryptionInfo> encryptionInfo);
	void merge(const FontEntry &fontEntry);

	bool operator == (const FontEntry &other) const;
	bool operator != (const FontEntry &other) const;

public:
	shared_ptr<FileInfo> Normal;
	shared_ptr<FileInfo> Bold;
	shared_ptr<FileInfo> Italic;
	shared_ptr<FileInfo> BoldItalic;
};

class FontMap {

public:
	void append(const std::string &family, bool bold, bool italic, const std::string &path, shared_ptr<FileEncryptionInfo> encryptionInfo);
	void merge(const FontMap &fontMap);
	shared_ptr<FontEntry> get(const std::string &family);

private:
	std::map<std::string,shared_ptr<FontEntry> > myMap;
};

#endif /* __FONTMAP_H__ */
