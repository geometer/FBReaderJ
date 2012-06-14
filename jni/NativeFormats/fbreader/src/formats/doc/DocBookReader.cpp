/*
 * Copyright (C) 2004-2010 Geometer Plus <contact@geometerplus.com>
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

#include <iostream>
#include <vector>
#include <string>

#include <ZLInputStream.h>
#include <ZLLogger.h>
#include <ZLFile.h>
#include <ZLStringUtil.h>

#include "DocBookReader.h"
#include "../../bookmodel/BookModel.h"
#include "../../library/Book.h"

#include "OleStorage.h"
#include "OleMainStream.h"

DocBookReader::DocBookReader(BookModel &model, const std::string &encoding) :
	OleStreamReader(encoding),
	myModelReader(model) {
	myReadState = READ_TEXT;
}

bool DocBookReader::readBook() {
	const ZLFile &file = myModelReader.model().book()->file();
	shared_ptr<ZLInputStream> stream = file.inputStream();
	if (stream.isNull()) {
		return false;
	}
	return readDocument(stream, file.size());
}

bool DocBookReader::readDocument(shared_ptr<ZLInputStream> inputStream, size_t streamSize) {
	static const std::string WORD_DOCUMENT = "WordDocument";

	if (inputStream.isNull() || !inputStream->open()) {
		return false;
	}
	myModelReader.setMainTextModel();
	myModelReader.pushKind(REGULAR);
	myModelReader.beginParagraph();

	shared_ptr<OleStorage> storage = new OleStorage;

	if (!storage->init(inputStream, streamSize)) {
		ZLLogger::Instance().println("DocBookReader", "Broken OLE file!");
		return false;
	}


	OleEntry wordDocumentEntry;
	bool result = storage->getEntryByName(WORD_DOCUMENT, wordDocumentEntry);
	if (!result) {
		return false;
	}

	OleMainStream oleStream(storage, wordDocumentEntry, inputStream);
	result = readStream(oleStream);
	if (!result) {
		return false;
	}

	myModelReader.insertEndOfTextParagraph();
	return true;
}

void DocBookReader::handleChar(ZLUnicodeUtil::Ucs2Char ucs2char) {
	if (myReadState == READ_FIELD && myReadFieldState == READ_FIELD_INFO) {
		myFieldInfoBuffer.push_back(ucs2char);
		return;
	}
	if (myReadState == READ_FIELD && myReadFieldState == DONT_READ_FIELD_TEXT) {
		return;
	}
	if (myReadState == READ_FIELD && myReadFieldState == READ_FIELD_TEXT && ucs2char == WORD_HORIZONTAL_TAB) {
		//to remove pagination from TOC (from doc saved in OpenOffice)
		myReadFieldState = DONT_READ_FIELD_TEXT;
		return;
	}
	std::string utf8String;
	ZLUnicodeUtil::Ucs2String ucs2String;
	ucs2String.push_back(ucs2char);
	ZLUnicodeUtil::ucs2ToUtf8(utf8String, ucs2String);
	if (!myModelReader.paragraphIsOpen()) {
		myModelReader.beginParagraph();
	}
	myModelReader.addData(utf8String);
}

void DocBookReader::handleHardLinebreak() {
	if (myModelReader.paragraphIsOpen()) {
		myModelReader.endParagraph();
	}
	myModelReader.beginParagraph();
	if (!myCurStyleEntry.isNull()) {
		myModelReader.addStyleEntry(*myCurStyleEntry);
	}
	for (size_t i = 0; i < myKindStack.size(); ++i) {
		myModelReader.addControl(myKindStack.at(i), true);
	}
}

void DocBookReader::handleParagraphEnd() {
	if (myModelReader.paragraphIsOpen()) {
		myModelReader.endParagraph();
	}
	myModelReader.beginParagraph();
	myCurStyleEntry = 0;
}

void DocBookReader::handlePageBreak() {
	if (myModelReader.paragraphIsOpen()) {
		myModelReader.endParagraph();
	}
	myCurStyleEntry = 0;
	myModelReader.insertEndOfSectionParagraph();
	myModelReader.beginParagraph();
}

void DocBookReader::handleTableSeparator() {
	handleChar(SPACE);
	handleChar(VERTICAL_LINE);
	handleChar(SPACE);
}

void DocBookReader::handleTableEndRow() {
	handleParagraphEnd();
}

void DocBookReader::handleFootNoteMark() {
	//TODO implement
}

void DocBookReader::handleStartField() {
	if (myReadState == READ_FIELD) { //for nested fields
		handleEndField();
	}
	myReadState = READ_FIELD;
	myReadFieldState = READ_FIELD_INFO;
	myHyperlinkTypeState = NO_HYPERLINK;
}

void DocBookReader::handleSeparatorField() {
	static const std::string HYPERLINK = "HYPERLINK";
//	static const std::string PAGE = "PAGE";
//	static const std::string PAGEREF = "PAGEREF";
//	static const std::string SHAPE = "SHAPE";
	static const std::string SPACE_DELIMETER = " ";
	static const std::string LOCAL_LINK = "\\l";
	static const std::string QUOTE = "\"";
	myReadFieldState = READ_FIELD_TEXT;
	myHyperlinkTypeState = NO_HYPERLINK;
	ZLUnicodeUtil::Ucs2String buffer = myFieldInfoBuffer;
	myFieldInfoBuffer.clear();
	std::string utf8String;
	ZLUnicodeUtil::ucs2ToUtf8(utf8String, buffer);
	ZLStringUtil::stripWhiteSpaces(utf8String);
	if (utf8String.empty()) {
		return;
	}
	std::vector<std::string> result = ZLStringUtil::split(utf8String, SPACE_DELIMETER);
	//TODO split function can returns empty string, maybe fix it
	std::vector<std::string> splitted;
	for (size_t i = 0; i < result.size(); ++i) {
		if (!result.at(i).empty()) {
			splitted.push_back(result.at(i));
		}
	}

	if (splitted.size() < 2 || splitted.at(0) != HYPERLINK) {
		myReadFieldState = DONT_READ_FIELD_TEXT;
		//to remove pagination from TOC and not hyperlink fields
		return;
	}

	if (splitted.at(1) == LOCAL_LINK) {
		std::string link = parseLink(buffer);
		if (!link.empty()) {
			myModelReader.addHyperlinkControl(INTERNAL_HYPERLINK, link);
			myHyperlinkTypeState = INT_HYPERLINK_INSERTED;
		}
	} else {
		std::string link = parseLink(buffer, true);
		if (!link.empty()) {
			myModelReader.addHyperlinkControl(EXTERNAL_HYPERLINK, link);
			myHyperlinkTypeState = EXT_HYPERLINK_INSERTED;
		}
	}
}

void DocBookReader::handleEndField() {
	myFieldInfoBuffer.clear();
	if (myReadState == READ_TEXT) {
		return;
	}
	if (myHyperlinkTypeState == EXT_HYPERLINK_INSERTED) {
		myModelReader.addControl(EXTERNAL_HYPERLINK, false);
	} else if (myHyperlinkTypeState == INT_HYPERLINK_INSERTED) {
		myModelReader.addControl(INTERNAL_HYPERLINK, false);
	}
	myReadState = READ_TEXT;
	myHyperlinkTypeState = NO_HYPERLINK;

}

void DocBookReader::handleStartOfHeading() {
	//heading can be, for example, a picture
	//TODO implement
}

void DocBookReader::handleOtherControlChar(ZLUnicodeUtil::Ucs2Char ucs2char) {
	if (ucs2char == WORD_SHORT_DEFIS) {
		handleChar(SHORT_DEFIS);
	} else if (ucs2char == WORD_SOFT_HYPHEN) {
		//skip
	} else if (ucs2char == WORD_HORIZONTAL_TAB) {
		handleChar(ucs2char);
	} else {
//		myTextBuffer.clear();
	}
}

void DocBookReader::handleFontStyle(unsigned int fontStyle) {
	if (myReadState == READ_FIELD && myReadFieldState == READ_FIELD_TEXT && myHyperlinkTypeState != NO_HYPERLINK) {
		//to fix bug with hyperlink, that's only bold and doesn't looks like hyperlink
		return;
	}
	while (!myKindStack.empty()) {
		myModelReader.addControl(myKindStack.back(), false);
		myKindStack.pop_back();
	}
	if (fontStyle & OleMainStream::CharInfo::BOLD) {
		myKindStack.push_back(BOLD);
	}
	if (fontStyle & OleMainStream::CharInfo::ITALIC) {
		myKindStack.push_back(ITALIC);
	}
	for (size_t i = 0; i < myKindStack.size(); ++i) {
		myModelReader.addControl(myKindStack.at(i), true);
	}
}

void DocBookReader::handleParagraphStyle(const OleMainStream::Style &styleInfo) {
	if (styleInfo.hasPageBreakBefore) {
		handlePageBreak();
	}
	shared_ptr<ZLTextStyleEntry> entry = new ZLTextStyleEntry();

	if (styleInfo.alignment == OleMainStream::Style::LEFT) {
		entry->setAlignmentType(ALIGN_JUSTIFY); //force justify align
	} else if (styleInfo.alignment == OleMainStream::Style::CENTER) {
		entry->setAlignmentType(ALIGN_CENTER);
	} else if (styleInfo.alignment == OleMainStream::Style::RIGHT) {
		entry->setAlignmentType(ALIGN_RIGHT);
	} else if (styleInfo.alignment == OleMainStream::Style::JUSTIFY) {
		entry->setAlignmentType(ALIGN_JUSTIFY);
	}

	//TODO in case, where style is heading, but size is small it works wrong
	ZLTextStyleEntry::SizeUnit unit = ZLTextStyleEntry::SIZE_UNIT_PERCENT;
	if (styleInfo.istd == OleMainStream::H1) {
		entry->setLength(ZLTextStyleEntry::LENGTH_FONT_SIZE, 140, unit);
	} else if (styleInfo.istd == OleMainStream::H2) {
		entry->setLength(ZLTextStyleEntry::LENGTH_FONT_SIZE, 120, unit);
	} else if (styleInfo.istd == OleMainStream::H3) {
		entry->setLength(ZLTextStyleEntry::LENGTH_FONT_SIZE, 110, unit);
	}

	myCurStyleEntry = entry;
	myModelReader.addStyleEntry(*myCurStyleEntry);

	//we should have the same font style, as for the previous paragraph, if it has the same istd
	if (myCurStyleInfo.istd != OleMainStream::ISTD_INVALID && myCurStyleInfo.istd == styleInfo.istd) {
		for (size_t i = 0; i < myKindStack.size(); ++i) {
			myModelReader.addControl(myKindStack.at(i), true);
		}
	} else {
		myKindStack.clear();
		handleFontStyle(styleInfo.charInfo.fontStyle); //fill by the fontstyle, that was got from Stylesheet
	}
	myCurStyleInfo = styleInfo;
}

void DocBookReader::handleBookmark(const std::string &name) {
	myModelReader.addHyperlinkLabel(name);
}

std::string DocBookReader::parseLink(ZLUnicodeUtil::Ucs2String s, bool urlencode) {
	//TODO add support for HYPERLINK like that:
	// [0x13] HYPERLINK "http://site.ru/some text" \t "_blank" [0x14] text [0x15]
	//Current implementation search for last QUOTE, so, it reads \t and _blank as part of link
	//Last quote searching is need to handle link like that:
	// [0x13] HYPERLINK "http://yandex.ru/yandsearch?text='some text' и "some text2"" [0x14] link text [0x15]

	static const ZLUnicodeUtil::Ucs2Char QUOTE = 0x22;
	size_t i, first = 0;
	//TODO maybe functions findFirstOf and findLastOf should be in ZLUnicodeUtil class
	for (i = 0; i < s.size(); ++i) {
		if (s.at(i) == QUOTE) {
			first = i;
			break;
		}
	}
	if (i == s.size()) {
		return std::string();
	}
	size_t j, last = 0;
	for (j = s.size(); j > 0 ; --j) {
		if (s.at(j - 1) == QUOTE) {
			last = j - 1;
			break;
		}
	}
	if (j == 0 || last == first) {
		return std::string();
	}

	ZLUnicodeUtil::Ucs2String link;
	for (size_t k = first + 1; k < last; ++k) {
		ZLUnicodeUtil::Ucs2Char ch = s.at(k);
		if (urlencode && ZLUnicodeUtil::isSpace(ch)) {
			//TODO maybe implement function for encoding all signs in url, not only spaces and quotes
			//TODO maybe add backslash support
			link.push_back('%');
			link.push_back('2');
			link.push_back('0');
		} else if (urlencode && ch == QUOTE) {
			link.push_back('%');
			link.push_back('2');
			link.push_back('2');
		} else {
			link.push_back(ch);
		}
	}
	std::string utf8String;
	ZLUnicodeUtil::ucs2ToUtf8(utf8String, link);
	return utf8String;
}

