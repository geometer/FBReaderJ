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

#ifndef __OLESTREAMREADER_H__
#define __OLESTREAMREADER_H__

#include <ZLUnicodeUtil.h>

#include "OleMainStream.h"

class OleStreamReader {

public:
	OleStreamReader();
	bool readDocument(shared_ptr<ZLInputStream> stream);

protected:
	virtual bool readStream(OleMainStream &stream) = 0;

	bool readNextPiece(OleMainStream &stream);

	virtual void dataHandler(const char *buffer, size_t len) = 0;
	virtual void ansiSymbolHandler(ZLUnicodeUtil::Ucs2Char symbol) = 0;
	virtual void footnoteHandler() = 0;

private:
	size_t myNextPieceNumber;
};

#endif /* __OLESTREAMREADER_H__ */
