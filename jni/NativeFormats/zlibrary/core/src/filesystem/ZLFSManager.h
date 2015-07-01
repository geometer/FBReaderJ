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

#ifndef __ZLFSMANAGER_H__
#define __ZLFSMANAGER_H__

#include <string>
#include <map>

#include <ZLFileInfo.h>
#include <ZLFile.h>

class ZLDir;
class ZLFSDir;
class ZLInputStream;
class ZLOutputStream;

class ZLFSManager {

public:
	static void deleteInstance();
	static ZLFSManager &Instance();

protected:
	static ZLFSManager *ourInstance;

protected:
	ZLFSManager();
	virtual ~ZLFSManager();

public:
	void normalize(std::string &path) const;
	virtual std::string resolveSymlink(const std::string &path) const = 0;

protected:
	virtual void normalizeRealPath(std::string &path) const = 0;
	virtual ZLInputStream *createPlainInputStream(const std::string &path) const = 0;
	virtual ZLOutputStream *createOutputStream(const std::string &path) const = 0;
	virtual ZLFSDir *createPlainDirectory(const std::string &path) const = 0;
	virtual ZLFSDir *createNewDirectory(const std::string &path) const = 0;
	virtual ZLFileInfo fileInfo(const std::string &path) const = 0;
	virtual bool removeFile(const std::string &path) const = 0;
	virtual std::string convertFilenameToUtf8(const std::string &name) const = 0;
	virtual std::string mimeType(const std::string &path) const = 0;

	virtual int findArchiveFileNameDelimiter(const std::string &path) const = 0;
	int findLastFileNameDelimiter(const std::string &path) const;
	virtual shared_ptr<ZLDir> rootDirectory() const = 0;
	virtual const std::string &rootDirectoryPath() const = 0;
	virtual std::string parentPath(const std::string &path) const = 0;

	virtual bool canRemoveFile(const std::string &path) const = 0;

private:
	std::map<std::string,ZLFile::ArchiveType> myForcedFiles;

friend class ZLFile;
friend class ZLDir;
};

inline ZLFSManager &ZLFSManager::Instance() { return *ourInstance; }
inline ZLFSManager::ZLFSManager() {}
inline ZLFSManager::~ZLFSManager() {}

#endif /* __ZLFSMANAGER_H__ */
