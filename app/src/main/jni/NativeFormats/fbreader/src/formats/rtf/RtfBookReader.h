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

#ifndef __RTFBOOKREADER_H__
#define __RTFBOOKREADER_H__

#include <vector>

#include "RtfReader.h"
#include "../../bookmodel/BookReader.h"

class ZLFile;

class BookModel;
class RtfImage;

class RtfBookReader : public RtfReader {

public:
	RtfBookReader(BookModel &model, const std::string &encoding);
	~RtfBookReader();

	bool readDocument(const ZLFile &file);

	bool characterDataHandler(std::string &str);
	void flushBuffer();

	void setEncoding(int code);
	void setAlignment();
	void switchDestination(DestinationType destination, bool on);
	void addCharData(const char *data, std::size_t len, bool convert);
	void insertImage(const std::string &mimeType, const std::string &fileName, std::size_t startOffset, std::size_t size);

	void setFontProperty(FontProperty property);
	void newParagraph();

private:
	BookReader myBookReader;

	std::string myOutputBuffer;

	int myImageIndex;
	int myFootnoteIndex;

	struct RtfBookReaderState {
		std::string Id;
		bool ReadText;
	};

	RtfBookReaderState myCurrentState;
	std::stack<RtfBookReaderState> myStateStack;
};

inline RtfBookReader::~RtfBookReader() {}

#endif /* __RTFBOOKREADER_H__ */
