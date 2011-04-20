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

#ifndef __ZLTEXTROWMEMORYALLOCATOR_H__
#define __ZLTEXTROWMEMORYALLOCATOR_H__

#include <vector>

class ZLTextRowMemoryAllocator {

public:
	ZLTextRowMemoryAllocator(const size_t rowSize);
	~ZLTextRowMemoryAllocator();

	char *allocate(size_t size);
	char *reallocateLast(char *ptr, size_t newSize);

private:
	const size_t myRowSize;
	size_t myCurrentRowSize;
	std::vector<char*> myPool;
	size_t myOffset;

private: // disable copying
	ZLTextRowMemoryAllocator(const ZLTextRowMemoryAllocator&);
	const ZLTextRowMemoryAllocator &operator = (const ZLTextRowMemoryAllocator&);
};

#endif /* __ZLTEXTROWMEMORYALLOCATOR_H__ */
