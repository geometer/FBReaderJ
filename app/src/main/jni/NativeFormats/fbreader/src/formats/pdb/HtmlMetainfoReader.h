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

#ifndef __HTMLMETAINFOREADER_H__
#define __HTMLMETAINFOREADER_H__

#include "../html/HtmlReader.h"

class Book;

class HtmlMetainfoReader : public HtmlReader {

public:
	enum ReadType {
		NONE = 0,
		TITLE = 1,
		AUTHOR = 2,
		TITLE_AND_AUTHOR = TITLE | AUTHOR,
		TAGS = 4,
		ALL = TITLE | AUTHOR | TAGS
	};

public:
	HtmlMetainfoReader(Book &book, ReadType readType);

private:
	void startDocumentHandler();
	void endDocumentHandler();

	bool tagHandler(const HtmlTag &tag);
	bool characterDataHandler(const char *text, size_t len, bool convert);

private:
	Book &myBook;
	const ReadType myReadType;

	bool myReadTitle;
	bool myReadAuthor;
	bool myReadTags;

	std::string myBuffer;
};

#endif /* __HTMLMETAINFOREADER_H__ */
