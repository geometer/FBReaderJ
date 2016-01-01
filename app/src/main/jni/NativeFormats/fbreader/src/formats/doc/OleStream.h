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

#ifndef __OLESTREAM_H__
#define __OLESTREAM_H__

#include <ZLFileImage.h>

#include "OleStorage.h"

class OleStream {

public:
	OleStream(shared_ptr<OleStorage> storage, OleEntry oleEntry, shared_ptr<ZLInputStream> stream);

public:
	bool open();
	std::size_t read(char *buffer, std::size_t maxSize);
	void close();

public:
	bool seek(unsigned int offset, bool absoluteOffset);
	std::size_t offset();

public:
	ZLFileImage::Blocks getBlockPieceInfoList(unsigned int offset, unsigned int size) const;
	static ZLFileImage::Blocks concatBlocks(const ZLFileImage::Blocks &blocks);
	std::size_t fileOffset();

public:
	bool eof() const;

protected:
	shared_ptr<OleStorage> myStorage;

	OleEntry myOleEntry;
	shared_ptr<ZLInputStream> myBaseStream;

	unsigned int myOleOffset;
};

#endif /* __OLESTREAM_H__ */
