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

#ifndef __OLESTREAMREADER_H__
#define __OLESTREAMREADER_H__

#include <ZLUnicodeUtil.h>

#include "OleMainStream.h"

class OleStreamReader {

public:
	OleStreamReader();
	bool readDocument(shared_ptr<ZLInputStream> stream, bool doReadFormattingData);

protected:
	virtual bool readStream(OleMainStream &stream) = 0;

	bool readNextPiece(OleMainStream &stream);

	virtual void ansiDataHandler(const char *buffer, std::size_t len) = 0;
	virtual void ucs2SymbolHandler(ZLUnicodeUtil::Ucs2Char symbol) = 0;
	virtual void footnotesStartHandler() = 0;

private:
	std::size_t myNextPieceNumber;
};

#endif /* __OLESTREAMREADER_H__ */
