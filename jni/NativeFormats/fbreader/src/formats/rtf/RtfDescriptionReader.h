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

#ifndef __RTFDESCRIPTIONREADER_H__
#define __RTFDESCRIPTIONREADER_H__

#include <string>

#include "RtfReader.h"

class Book;

class RtfDescriptionReader : public RtfReader {

public:
	RtfDescriptionReader(Book &book);
	~RtfDescriptionReader();

	bool readDocument(const ZLFile &file);

	void setEncoding(int code);
	void setAlignment();
	void switchDestination(DestinationType destination, bool on);
	void addCharData(const char *data, std::size_t len, bool convert);
	void insertImage(const std::string &mimeType, const std::string &fileName, std::size_t startOffset, std::size_t size);

	void setFontProperty(FontProperty property);
	void newParagraph();

private:
	Book &myBook;

	bool myDoRead;
	std::string myBuffer;
};

inline RtfDescriptionReader::~RtfDescriptionReader() {}

#endif /* __RTFDESCRIPTIONREADER_H__ */
