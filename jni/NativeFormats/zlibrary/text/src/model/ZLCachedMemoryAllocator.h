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

#ifndef __ZLCACHEDMEMORYALLOCATOR_H__
#define __ZLCACHEDMEMORYALLOCATOR_H__

#include <vector>

#include <ZLUnicodeUtil.h>

class ZLCachedMemoryAllocator {

public:
	ZLCachedMemoryAllocator(const std::size_t rowSize, const std::string &directoryName, const std::string &fileExtension);
	~ZLCachedMemoryAllocator();

	char *allocate(std::size_t size);
	char *reallocateLast(char *ptr, std::size_t newSize);

	void flush();

	static char *writeUInt16(char *ptr, uint16_t value);
	static char *writeUInt32(char *ptr, uint32_t value);
	static char *writeString(char *ptr, const ZLUnicodeUtil::Ucs2String &str);
	static uint16_t readUInt16(const char *ptr);
	static uint32_t readUInt32(const char *ptr);

public:
	const std::string &directoryName() const;
	const std::string &fileExtension() const;
	std::size_t blocksNumber() const;
	std::size_t currentBytesOffset() const;
	bool failed() const;

private:
	std::string makeFileName(std::size_t index);
	void writeCache(std::size_t blockLength);

private:
	const std::size_t myRowSize;
	std::size_t myCurrentRowSize;
	std::vector<char*> myPool;
	std::size_t myOffset;

	bool myHasChanges;
	bool myFailed;

	const std::string myDirectoryName;
	const std::string myFileExtension;

private: // disable copying
	ZLCachedMemoryAllocator(const ZLCachedMemoryAllocator&);
	const ZLCachedMemoryAllocator &operator = (const ZLCachedMemoryAllocator&);
};

inline const std::string &ZLCachedMemoryAllocator::directoryName() const { return myDirectoryName; }
inline const std::string &ZLCachedMemoryAllocator::fileExtension() const { return myFileExtension; }
inline std::size_t ZLCachedMemoryAllocator::blocksNumber() const { return myPool.size(); }
inline std::size_t ZLCachedMemoryAllocator::currentBytesOffset() const { return myOffset; }
inline bool ZLCachedMemoryAllocator::failed() const { return myFailed; }

inline char *ZLCachedMemoryAllocator::writeUInt16(char *ptr, uint16_t value) {
	*ptr++ = value;
	*ptr++ = value >> 8;
	return ptr;
}
inline char *ZLCachedMemoryAllocator::writeUInt32(char *ptr, uint32_t value) {
	*ptr++ = value;
	value >>= 8;
	*ptr++ = value;
	value >>= 8;
	*ptr++ = value;
	value >>= 8;
	*ptr++ = value;
	return ptr;
}
inline char *ZLCachedMemoryAllocator::writeString(char *ptr, const ZLUnicodeUtil::Ucs2String &str) {
	const std::size_t size = str.size();
	writeUInt16(ptr, size);
	memcpy(ptr + 2, &str.front(), size * 2);
	return ptr + size * 2 + 2;
}

inline uint16_t ZLCachedMemoryAllocator::readUInt16(const char *ptr) {
	const uint8_t *tmp = (const uint8_t*)ptr;
	return *tmp + ((uint16_t)*(tmp + 1) << 8);
}
inline uint32_t ZLCachedMemoryAllocator::readUInt32(const char *ptr) {
	const uint8_t *tmp = (const uint8_t*)ptr;
	return *tmp
		+ ((uint32_t)*(tmp + 1) << 8)
		+ ((uint32_t)*(tmp + 2) << 16)
		+ ((uint32_t)*(tmp + 3) << 24);
}

#endif /* __ZLCACHEDMEMORYALLOCATOR_H__ */
