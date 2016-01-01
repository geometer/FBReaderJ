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

#ifndef __TXTBOOKREADER_H__
#define __TXTBOOKREADER_H__

#include <stack>

#include "TxtReader.h"
#include "PlainTextFormat.h"
#include "../../bookmodel/BookReader.h"

class BookModel;

class TxtBookReader : public TxtReader, public BookReader {

public:
	TxtBookReader(BookModel &model, const PlainTextFormat &format, const std::string &encoding);
	~TxtBookReader();

protected:
	void startDocumentHandler();
	void endDocumentHandler();

	bool characterDataHandler(std::string &str);
	bool newLineHandler();

private:
	void internalEndParagraph();

private:
	const PlainTextFormat &myFormat;

	int myLineFeedCounter;
	bool myInsideContentsParagraph;
	bool myLastLineIsEmpty;
	bool myNewLine;
	int mySpaceCounter;
};

inline TxtBookReader::~TxtBookReader() {}

#endif /* __TXTBOOKREADER_H__ */
