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

#ifndef __FB2METAINFOREADER_H__
#define __FB2METAINFOREADER_H__

#include <string>

#include "FB2Reader.h"

class Book;

class FB2MetaInfoReader : public FB2Reader {

public:
	FB2MetaInfoReader(Book &book);
	bool readMetaInfo();

	void startElementHandler(int tag, const char **attributes);
	void endElementHandler(int tag);
	void characterDataHandler(const char *text, size_t len);

private:
	Book &myBook;

	bool myReturnCode;

	enum {
		READ_NOTHING,
		READ_SOMETHING,
		READ_TITLE,
		READ_AUTHOR,
		READ_AUTHOR_NAME_0,
		READ_AUTHOR_NAME_1,
		READ_AUTHOR_NAME_2,
		READ_LANGUAGE,
		READ_GENRE
	} myReadState;

	std::string myAuthorNames[3];
	std::string myBuffer;
};

#endif /* __FB2METAINFOREADER_H__ */
