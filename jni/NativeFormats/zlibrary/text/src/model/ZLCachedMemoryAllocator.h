/*
 * Copyright (C) 2004-2012 Geometer Plus <contact@geometerplus.com>
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

class ZLCachedMemoryAllocator {

public:
	ZLCachedMemoryAllocator(const size_t rowSize, const std::string &directoryName, const std::string &fileExtension);
	~ZLCachedMemoryAllocator();

	char *allocate(size_t size);
	char *reallocateLast(char *ptr, size_t newSize);

	void flush();

	static void writeUInt16(char *ptr, uint16_t value);
	static void writeUInt32(char *ptr, uint32_t value);
	static uint16_t readUInt16(const char *ptr);
	static uint32_t readUInt32(const char *ptr);

public:
	const std::string &directoryName() const;
	const std::string &fileExtension() const;
	size_t blocksNumber() const;
	size_t currentBytesOffset() const;

private:
	std::string makeFileName(size_t index);
	void writeCache(size_t blockLength);

private:
	const size_t myRowSize;
	size_t myCurrentRowSize;
	std::vector<char*> myPool;
	size_t myOffset;

	bool myHasChanges;

	const std::string myDirectoryName;
	const std::string myFileExtension;

private: // disable copying
	ZLCachedMemoryAllocator(const ZLCachedMemoryAllocator&);
	const ZLCachedMemoryAllocator &operator = (const ZLCachedMemoryAllocator&);
};

inline const std::string &ZLCachedMemoryAllocator::directoryName() const { return myDirectoryName; }
inline const std::string &ZLCachedMemoryAllocator::fileExtension() const { return myFileExtension; }
inline size_t ZLCachedMemoryAllocator::blocksNumber() const { return myPool.size(); }
inline size_t ZLCachedMemoryAllocator::currentBytesOffset() const { return myOffset; }

inline void ZLCachedMemoryAllocator::writeUInt16(char *ptr, uint16_t value) {
	*ptr++ = value;
	*ptr = value >> 8;
}
inline void ZLCachedMemoryAllocator::writeUInt32(char *ptr, uint32_t value) {
	*ptr++ = value;
	value >>= 8;
	*ptr++ = value;
	value >>= 8;
	*ptr++ = value;
	*ptr = value >> 8;
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
