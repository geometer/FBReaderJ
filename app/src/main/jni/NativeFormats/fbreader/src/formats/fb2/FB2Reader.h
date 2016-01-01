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

#ifndef __FB2READER_H__
#define __FB2READER_H__

#include <ZLXMLReader.h>

class FB2Reader : public ZLXMLReader {

public:
	struct Tag {
		const char *tagName;
		int tagCode;
	};

protected:
	virtual int tag(const char *name);

	virtual void startElementHandler(int tag, const char **attributes) = 0;
	virtual void endElementHandler(int tag) = 0;

private:
	void startElementHandler(const char *tag, const char **attributes);
	void endElementHandler(const char *tag);

	void collectExternalEntities(std::map<std::string,std::string> &entityMap);

public:
	enum TagCode {
		_P,
		_UL,
		_OL,
		_LI,
		_SUBTITLE,
		_CITE,
		_TEXT_AUTHOR,
		_DATE,
		_SECTION,
		_V,
		_TITLE,
		_POEM,
		_STANZA,
		_EPIGRAPH,
		_ANNOTATION,
		_SUB,
		_SUP,
		_CODE,
		_STRIKETHROUGH,
		_STRONG,
		_EMPHASIS,
		_A,
		_IMAGE,
		_BINARY,
		_DESCRIPTION,
		_BODY,
		_EMPTY_LINE,
		_TITLE_INFO,
		_BOOK_TITLE,
		_AUTHOR,
		_LANG,
		_FIRST_NAME,
		_MIDDLE_NAME,
		_LAST_NAME,
		_COVERPAGE,
		_SEQUENCE,
		_GENRE,
		_DOCUMENT_INFO,
		_ID,
		_UNKNOWN
	};

protected:
	FB2Reader();
	~FB2Reader();

protected:
	const FullNamePredicate myHrefPredicate;
	const BrokenNamePredicate myBrokenHrefPredicate;
};

inline FB2Reader::~FB2Reader() {}

#endif /* __FB2READER_H__ */
