/*
 * Copyright (C) 2004-2010 Geometer Plus <contact@geometerplus.com>
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

#include <algorithm>
#include <cctype>

#include "ZLTar.h"
#include "../ZLFile.h"

struct ZLTarHeader {
	std::string Name;
	size_t Size;
	bool IsRegularFile;
	size_t DataOffset;

	bool read(ZLInputStream &stream);
	void erase();
};

class ZLTarHeaderCache : public ZLUserData {

public:
	static const ZLTarHeaderCache &cache(ZLInputStream &baseStream);

public:
	ZLTarHeaderCache(ZLInputStream &baseStream);
	ZLTarHeader header(const std::string &entryName) const;
	void collectFileNames(std::vector<std::string> &names) const;

private:
	std::map<std::string,ZLTarHeader> myHeaderMap;
};

ZLTarHeaderCache::ZLTarHeaderCache(ZLInputStream &baseStream) {
	if (!baseStream.open()) {
		return;
	}

	ZLTarHeader header;
	while (header.read(baseStream)) {
		if (header.IsRegularFile) {
			myHeaderMap[header.Name] = header;
		}
		baseStream.seek((header.Size + 0x1ff) & -0x200, false);
		header.erase();
	}
	baseStream.close();
}

ZLTarHeader ZLTarHeaderCache::header(const std::string &entryName) const {
	std::map<std::string,ZLTarHeader>::const_iterator it = myHeaderMap.find(entryName);
	return (it != myHeaderMap.end()) ? it->second : ZLTarHeader();
}

void ZLTarHeaderCache::collectFileNames(std::vector<std::string> &names) const {
	for (std::map<std::string,ZLTarHeader>::const_iterator it = myHeaderMap.begin(); it != myHeaderMap.end(); ++it) {
		names.push_back(it->first);
	}
}

const ZLTarHeaderCache &ZLTarHeaderCache::cache(ZLInputStream &baseStream) {
	static const std::string tarHeaderMapKey = "tarHeaderMap";
	shared_ptr<ZLUserData> data = baseStream.getUserData(tarHeaderMapKey);
	if (data.isNull()) {
		data = new ZLTarHeaderCache(baseStream);
		baseStream.addUserData(tarHeaderMapKey, data);
	}
	return (const ZLTarHeaderCache&)*data;
}

bool ZLTarHeader::read(ZLInputStream &stream) {
	size_t startOffset = stream.offset();

	char fileName[101];
	stream.read(fileName, 100);
	if (fileName[0] == '\0') {
		return false;
	}
	fileName[100] = '\0';
	if (Name.empty()) {
		Name = fileName;
	}

	stream.seek(24, false);
	
	char fileSizeString[12];
	stream.read(fileSizeString, 12);
	Size = 0;
	for (int i = 0; i < 12; ++i) {
		if (!isdigit(fileSizeString[i])) {
			break;
		}
		Size *= 8;
		Size += fileSizeString[i] - '0';
	}
	
	stream.seek(20, false);
	char linkFlag;
	stream.read(&linkFlag, 1);
	
	IsRegularFile = (linkFlag == '\0') || (linkFlag == '0');

	stream.seek(355, false);
	
	if (((linkFlag == 'L') || (linkFlag == 'K')) && (Name == "././@LongLink") && (Size < 10240)) {
		Name.erase();
		Name.append(Size - 1, '\0');
		stream.read(const_cast<char*>(Name.data()), Size - 1);
		const int skip = 512 - (Size & 0x1ff);
		stream.seek(skip + 1, false);
		return (stream.offset() == startOffset + Size + skip + 512) && read(stream);
	} else {
		DataOffset = stream.offset();
		return DataOffset == startOffset + 512;
	}
}

void ZLTarHeader::erase() {
	Name.erase();
}

ZLTarInputStream::ZLTarInputStream(shared_ptr<ZLInputStream> &base, const std::string &name) : myBaseStream(new ZLInputStreamDecorator(base)), myCompressedFileName(name) {
}

ZLTarInputStream::~ZLTarInputStream() {
	close();
}

bool ZLTarInputStream::open() {
	close();
	if (!myBaseStream->open()) {
		return false;
	}

	const ZLTarHeaderCache &cache = ZLTarHeaderCache::cache(*myBaseStream);
	const ZLTarHeader header = cache.header(myCompressedFileName);
	if (header.Name.empty()) {
		return false;
	}

	if (!myBaseStream->open()) {
		return false;
	}
	myOffset = 0;
	myCompressedFileSize = header.Size;
	myBaseStream->seek(header.DataOffset, true);
	return true;
}

size_t ZLTarInputStream::read(char *buffer, size_t maxSize) {
	maxSize = std::min(maxSize, (size_t)(myCompressedFileSize - myOffset));
	size_t size = myBaseStream->read(buffer, maxSize);
	myOffset += size;
	return size;
}

void ZLTarInputStream::close() {
	myBaseStream->close();
}

void ZLTarInputStream::seek(int offset, bool absoluteOffset) {
	if (absoluteOffset) {
		offset -= myOffset;
	}
	offset = std::max(offset, -(int)myOffset);
	myBaseStream->seek(offset, false);
	myOffset += offset;
}

size_t ZLTarInputStream::offset() const {
	return myOffset;
}

size_t ZLTarInputStream::sizeOfOpened() {
	return myCompressedFileSize;
}

void ZLTarDir::collectFiles(std::vector<std::string> &names, bool) {
	shared_ptr<ZLInputStream> stream = ZLFile(path()).inputStream();
	const ZLTarHeaderCache &cache = ZLTarHeaderCache::cache(*stream);
	cache.collectFileNames(names);
}

std::string ZLTarDir::delimiter() const {
	return ":";
}
