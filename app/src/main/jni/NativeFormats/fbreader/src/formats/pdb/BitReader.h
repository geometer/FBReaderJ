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

#ifndef __BITREADER_H__
#define __BITREADER_H__

class BitReader {

public:
	BitReader(const unsigned char* data, size_t size);
	~BitReader();

	unsigned long long peek(size_t n);
	bool eat(size_t n);
	size_t left() const;

private:
	unsigned char *myData;
	size_t myOffset;
	const size_t myLength;
};

#endif //__BITREADER_H__
