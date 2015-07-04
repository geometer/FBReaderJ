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

#ifndef __DOCBOOKREADER_H__
#define __DOCBOOKREADER_H__

#include <vector>

#include <shared_ptr.h>
#include <ZLFile.h>
#include <ZLTextStyleEntry.h>
#include <ZLEncodingConverter.h>

#include "../../bookmodel/BookReader.h"

#include "OleMainStream.h"
#include "OleStreamParser.h"
#include "DocAnsiConverter.h"

class DocBookReader : public OleStreamParser {

public:
	DocBookReader(BookModel &model, const std::string &encoding);
	~DocBookReader();
	bool readBook();

private:
	void ansiDataHandler(const char *buffer, std::size_t len);
	void ucs2SymbolHandler(ZLUnicodeUtil::Ucs2Char symbol);
	void footnotesStartHandler();

	void handleChar(ZLUnicodeUtil::Ucs2Char ucs2char);
	void handleHardLinebreak();
	void handleParagraphEnd();
	void handlePageBreak();
	void handleTableSeparator();
	void handleTableEndRow();
	void handleFootNoteMark();
	void handleStartField();
	void handleSeparatorField();
	void handleEndField();
	void handleImage(const ZLFileImage::Blocks &blocks);
	void handleOtherControlChar(ZLUnicodeUtil::Ucs2Char ucs2char);

	//formatting:
	void handleFontStyle(unsigned int fontStyle);
	void handleParagraphStyle(const OleMainStream::Style &styleInfo);
	void handleBookmark(const std::string &name);

private:
	static std::string parseLink(ZLUnicodeUtil::Ucs2String s, bool urlencode = false);

private:
	BookReader myModelReader;

	ZLUnicodeUtil::Ucs2String myFieldInfoBuffer;

	enum {
		READ_FIELD,
		READ_TEXT
	} myReadState;

	enum {
		READ_FIELD_TEXT,
		DONT_READ_FIELD_TEXT,
		READ_FIELD_INFO
	} myReadFieldState;

	//maybe it should be flag?
	enum {
		NO_HYPERLINK,
		EXT_HYPERLINK_INSERTED,
		INT_HYPERLINK_INSERTED
	} myHyperlinkTypeState;

	//formatting
	std::vector<FBTextKind> myKindStack;
	shared_ptr<ZLTextStyleEntry> myCurrentStyleEntry;
	OleMainStream::Style myCurrentStyleInfo;
	unsigned int myPictureCounter;

	const std::string myEncoding;
	DocAnsiConverter myConverter;
};

inline DocBookReader::~DocBookReader() {}

#endif /* __DOCBOOKREADER_H__ */
