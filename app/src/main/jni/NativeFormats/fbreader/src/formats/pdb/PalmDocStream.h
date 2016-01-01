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

#ifndef __PALMDOCSTREAM_H__
#define __PALMDOCSTREAM_H__

#include "PalmDocLikeStream.h"

class ZLFile;
class HuffDecompressor;

class PalmDocStream : public PalmDocLikeStream {

protected:
	PalmDocStream(const ZLFile &file);

public:
	~PalmDocStream();
	
	std::pair<int,int> imageLocation(const PdbHeader &header, int index) const;
	bool hasExtraSections() const;

private:
	bool processRecord();
	bool processZeroRecord();

protected:
	unsigned long  myTextLength;

private:
	unsigned short myCompressionVersion;
	unsigned short myTextRecordNumber;
	unsigned short myImageStartIndex;

	shared_ptr<HuffDecompressor> myHuffDecompressorPtr;
};

class PalmDocContentStream : public PalmDocStream {

public:
	PalmDocContentStream(const ZLFile &file);

private:
	size_t sizeOfOpened();
};

class PalmDocCssStream : public PalmDocStream {

public:
	PalmDocCssStream(const ZLFile &file);

private:
	bool open();
	size_t sizeOfOpened();
	size_t offset() const;
};

#endif /* __PALMDOCSTREAM_H__ */
