/*
 * Copyright (C) 2004-2014 Geometer Plus <contact@geometerplus.com>
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

#ifndef __OEBMETAINFOREADER_H__
#define __OEBMETAINFOREADER_H__

#include <vector>

#include <ZLXMLReader.h>

class Book;

class OEBMetaInfoReader : public ZLXMLReader {

public:
	OEBMetaInfoReader(Book &book);
	bool readMetaInfo(const ZLFile &file);

	void startElementHandler(const char *tag, const char **attributes);
	void endElementHandler(const char *tag);
	void characterDataHandler(const char *text, std::size_t len);
	bool processNamespaces() const;
	const std::vector<std::string> &externalDTDs() const;

private:
	bool testDCTag(const std::string &name, const std::string &tag) const;
	bool isNSName(const std::string &fullName, const std::string &shortName, const std::string &fullNSId) const;

private:
	Book &myBook;

	enum {
		READ_NONE,
		READ_METADATA,
		READ_AUTHOR,
		READ_AUTHOR2,
		READ_TITLE,
		READ_SUBJECT,
		READ_LANGUAGE,
		READ_IDENTIFIER,
	} myReadState;

	std::string myIdentifierScheme;
	std::string myBuffer;
	std::vector<std::string> myAuthorList;
	std::vector<std::string> myAuthorList2;
};

#endif /* __OEBMETAINFOREADER_H__ */
