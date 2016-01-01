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

#ifndef __ZLZIP_H__
#define __ZLZIP_H__

#include <map>

#include <shared_ptr.h>

#include "../ZLInputStream.h"
#include "../ZLDir.h"

class ZLZDecompressor;
class ZLFile;

class ZLZipEntryCache {

public:
	static shared_ptr<ZLZipEntryCache> cache(const std::string &containerName, ZLInputStream &containerStream);

private:
	static const std::size_t ourStorageSize;
	static shared_ptr<ZLZipEntryCache> *ourStoredCaches;
	static std::size_t ourIndex;

public:
	struct Info {
		Info();

		int Offset;
		int CompressionMethod;
		int CompressedSize;
		int UncompressedSize;
	};

private:
	ZLZipEntryCache(const std::string &containerName, ZLInputStream &containerStream);

public:
	Info info(const std::string &entryName) const;
	void collectFileNames(std::vector<std::string> &names) const;

private:
	bool isValid() const;

private:
	const std::string myContainerName;
	std::size_t myLastModifiedTime;
	std::map<std::string,Info> myInfoMap;
};

class ZLZipInputStream : public ZLInputStream {

private:
	ZLZipInputStream(shared_ptr<ZLInputStream> base, const std::string &baseName, const std::string &entryName);

public:
	~ZLZipInputStream();
	bool open();
	std::size_t read(char *buffer, std::size_t maxSize);
	void close();

	void seek(int offset, bool absoluteOffset);
	std::size_t offset() const;
	std::size_t sizeOfOpened();

private:
	shared_ptr<ZLInputStream> myBaseStream;
	std::string myBaseName;
	std::string myEntryName;
	bool myIsOpen;
	bool myIsDeflated;

	std::size_t myUncompressedSize;
	std::size_t myAvailableSize;
	std::size_t myOffset;

	shared_ptr<ZLZDecompressor> myDecompressor;

friend class ZLFile;
};

class ZLGzipInputStream : public ZLInputStream {

private:
	ZLGzipInputStream(shared_ptr<ZLInputStream> stream);

public:
	~ZLGzipInputStream();
	bool open();
	std::size_t read(char *buffer, std::size_t maxSize);
	void close();

	void seek(int offset, bool absoluteOffset);
	std::size_t offset() const;
	std::size_t sizeOfOpened();

private:
	shared_ptr<ZLInputStream> myBaseStream;
	std::size_t myFileSize;

	std::size_t myOffset;

	shared_ptr<ZLZDecompressor> myDecompressor;

friend class ZLFile;
};

class ZLZipDir : public ZLDir {

private:
	ZLZipDir(const std::string &name);

public:
	~ZLZipDir();
	//void collectSubDirs(std::vector<std::string>&, bool);
	void collectFiles(std::vector<std::string> &names, bool includeSymlinks);

protected:
	std::string delimiter() const;

friend class ZLFile;
};

inline ZLZipDir::ZLZipDir(const std::string &name) : ZLDir(name) {}
inline ZLZipDir::~ZLZipDir() {}
//inline void ZLZipDir::collectSubDirs(std::vector<std::string>&, bool) {}

#endif /* __ZLZIP_H__ */
