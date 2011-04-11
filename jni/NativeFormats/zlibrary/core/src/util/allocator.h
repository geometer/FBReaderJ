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

#ifndef __ALLOCATOR_H__
#define __ALLOCATOR_H__

#include <vector>

template<size_t ObjectSize, size_t PoolSize> class Allocator {
	
public:
	Allocator();
	~Allocator();

	void *allocate();
	void free(void *ptr);

private:
	void addPool();
	
private:
	std::vector<void*> myPools;
	void *myFirstUnused;
	void *myLastUnused;
};

template<size_t ObjectSize, size_t PoolSize> 
inline Allocator<ObjectSize,PoolSize>::Allocator() {
	addPool();
}

template<size_t ObjectSize, size_t PoolSize> 
inline Allocator<ObjectSize,PoolSize>::~Allocator() {
	for (std::vector<void*>::const_iterator it = myPools.begin(); it != myPools.end(); ++it) {
		delete[] (char*)*it;
	}
}

template<size_t ObjectSize, size_t PoolSize> 
inline void Allocator<ObjectSize,PoolSize>::addPool() {
	char *pool = new char[ObjectSize * PoolSize];
	myFirstUnused = (void*)pool;
	myLastUnused = (void*)(pool + ObjectSize * (PoolSize - 1));
	for (size_t i = 0; i < PoolSize - 1; ++i) {
		*(void**)(pool + ObjectSize * i) = pool + ObjectSize * (i + 1);
	}
	myPools.push_back(myFirstUnused);
}

template<size_t ObjectSize, size_t PoolSize> 
void *Allocator<ObjectSize,PoolSize>::allocate() {
	void *ptr = myFirstUnused;
	if (myFirstUnused == myLastUnused) {
		addPool();
	} else {
		myFirstUnused = *(void**)myFirstUnused;
	}
	return ptr;
}

template<size_t ObjectSize, size_t PoolSize> 
void Allocator<ObjectSize,PoolSize>::free(void *ptr) {
	*(void**)myLastUnused = ptr;
	myLastUnused = ptr;
}

#endif /* __ALLOCATOR_H__ */
