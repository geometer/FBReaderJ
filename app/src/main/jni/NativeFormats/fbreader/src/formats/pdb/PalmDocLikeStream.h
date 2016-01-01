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

#ifndef __PALMDOCLIKESTREAM_H__
#define __PALMDOCLIKESTREAM_H__

#include "PdbStream.h"

class ZLFile;

class PalmDocLikeStream : public PdbStream {

public:
	enum ErrorCode {
		ERROR_NONE,
		ERROR_UNKNOWN,
		ERROR_COMPRESSION,
		ERROR_ENCRYPTION,
	};

public:
	PalmDocLikeStream(const ZLFile &file);
	~PalmDocLikeStream();
	bool open();
	
	ErrorCode errorCode() const;
	//std::pair<int,int> imageLocation(int index);
	//bool hasExtraSections() const;

protected:
	bool fillBuffer();

private:
	virtual bool processRecord() = 0;
	virtual bool processZeroRecord() = 0;

protected: 
	unsigned short myMaxRecordSize;
	size_t myRecordIndex;
	size_t myMaxRecordIndex;

	ErrorCode myErrorCode;
};

#endif /* __PALMDOCLIKESTREAM_H__ */
