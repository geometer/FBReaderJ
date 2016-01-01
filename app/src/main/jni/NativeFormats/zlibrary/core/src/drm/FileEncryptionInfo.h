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

#ifndef __FILEENCRYPTIONINFO_H__
#define __FILEENCRYPTIONINFO_H__

#include <map>
#include <string>

#include <shared_ptr.h>
#include <ZLDir.h>

class EncryptionMethod {

public:
	static const std::string UNSUPPORTED;
	static const std::string EMBEDDING;
	static const std::string MARLIN;
	static const std::string KINDLE;
};

class FileEncryptionInfo {

public:
	FileEncryptionInfo(const std::string &uri, const std::string &method, const std::string &algorithm, const std::string &contentId);

public:
	const std::string Uri;
	const std::string Method;
	const std::string Algorithm;
	const std::string ContentId;
};

class EncryptionMap {

public:
	void addInfo(const ZLDir &dir, shared_ptr<FileEncryptionInfo> info);
	shared_ptr<FileEncryptionInfo> info(const std::string &path) const;

private:
	std::map<std::string,shared_ptr<FileEncryptionInfo> > myPathToInfo;
};

#endif /* __FILEENCRYPTIONINFO_H__ */
