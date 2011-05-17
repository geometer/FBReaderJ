/*
 * Copyright (C) 2004-2011 Geometer Plus <contact@geometerplus.com>
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

#endif /* __ZLCACHEDMEMORYALLOCATOR_H__ */
