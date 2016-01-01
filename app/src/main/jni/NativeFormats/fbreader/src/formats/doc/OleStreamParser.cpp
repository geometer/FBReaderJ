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

//#include <cctype>
//#include <cstring>

#include <ZLLogger.h>

#include "OleMainStream.h"
#include "OleUtil.h"
#include "OleStreamParser.h"

//word's control chars:
const ZLUnicodeUtil::Ucs2Char OleStreamParser::WORD_FOOTNOTE_MARK = 0x0002;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::WORD_TABLE_SEPARATOR = 0x0007;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::WORD_HORIZONTAL_TAB = 0x0009;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::WORD_HARD_LINEBREAK = 0x000b;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::WORD_PAGE_BREAK = 0x000c;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::WORD_END_OF_PARAGRAPH = 0x000d;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::WORD_MINUS = 0x001e;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::WORD_SOFT_HYPHEN = 0x001f;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::WORD_START_FIELD = 0x0013;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::WORD_SEPARATOR_FIELD = 0x0014;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::WORD_END_FIELD = 0x0015;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::WORD_ZERO_WIDTH_UNBREAKABLE_SPACE = 0xfeff;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::INLINE_IMAGE = 0x0001;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::FLOAT_IMAGE = 0x0008;

//unicode values:
const ZLUnicodeUtil::Ucs2Char OleStreamParser::NULL_SYMBOL = 0x0;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::FILE_SEPARATOR = 0x1c;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::LINE_FEED = 0x000a;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::SOFT_HYPHEN = 0xad;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::SPACE = 0x20;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::MINUS = 0x2D;
const ZLUnicodeUtil::Ucs2Char OleStreamParser::VERTICAL_LINE = 0x7C;

OleStreamParser::OleStreamParser() {
	myCurBufferPosition = 0;

	myCurCharPos = 0;
	myNextStyleInfoIndex = 0;
	myNextCharInfoIndex = 0;
	myNextBookmarkIndex = 0;
	myNextInlineImageInfoIndex = 0;
	myNextFloatImageInfoIndex = 0;
}

bool OleStreamParser::readStream(OleMainStream &oleMainStream) {
	ZLUnicodeUtil::Ucs2Char ucs2char;
	bool tabMode = false;
	while (getUcs2Char(oleMainStream, ucs2char)) {
		if (tabMode) {
			tabMode = false;
			if (ucs2char == WORD_TABLE_SEPARATOR) {
				handleTableEndRow();
				continue;
			} else {
				handleTableSeparator();
			}
		}

		if (ucs2char < 32) {
			switch (ucs2char) {
				case NULL_SYMBOL:
					break;
				case WORD_HARD_LINEBREAK:
					handleHardLinebreak();
					break;
				case WORD_END_OF_PARAGRAPH:
				case WORD_PAGE_BREAK:
					handleParagraphEnd();
					break;
				case WORD_TABLE_SEPARATOR:
					tabMode = true;
					break;
				case WORD_FOOTNOTE_MARK:
					handleFootNoteMark();
					break;
				case WORD_START_FIELD:
					handleStartField();
					break;
				case WORD_SEPARATOR_FIELD:
					handleSeparatorField();
					break;
				case WORD_END_FIELD:
					handleEndField();
					break;
				case INLINE_IMAGE:
				case FLOAT_IMAGE:
					break;
				default:
					handleOtherControlChar(ucs2char);
					break;
			}
		} else if (ucs2char == WORD_ZERO_WIDTH_UNBREAKABLE_SPACE) {
			continue; //skip
		} else {
			handleChar(ucs2char);
		}
	}

	return true;
}

bool OleStreamParser::getUcs2Char(OleMainStream &stream, ZLUnicodeUtil::Ucs2Char &ucs2char) {
	while (myCurBufferPosition >= myBuffer.size()) {
		myBuffer.clear();
		myCurBufferPosition = 0;
		if (!readNextPiece(stream)) {
			return false;
		}
	}
	ucs2char = myBuffer.at(myCurBufferPosition++);
	processStyles(stream);

	switch (ucs2char) {
		case INLINE_IMAGE:
			processInlineImage(stream);
			break;
		case FLOAT_IMAGE:
			processFloatImage(stream);
			break;
	}
	++myCurCharPos;
	return true;
}

void OleStreamParser::processInlineImage(OleMainStream &stream) {
	const OleMainStream::InlineImageInfoList &imageInfoList = stream.getInlineImageInfoList();
	if (imageInfoList.empty()) {
		return;
	}
	//seek to curCharPos, because not all entries are real pictures
	while(myNextInlineImageInfoIndex < imageInfoList.size() && imageInfoList.at(myNextInlineImageInfoIndex).first < myCurCharPos) {
		++myNextInlineImageInfoIndex;
	}
	while (myNextInlineImageInfoIndex < imageInfoList.size() && imageInfoList.at(myNextInlineImageInfoIndex).first == myCurCharPos) {
		OleMainStream::InlineImageInfo info = imageInfoList.at(myNextInlineImageInfoIndex).second;
		ZLFileImage::Blocks list = stream.getInlineImage(info.DataPosition);
		if (!list.empty()) {
			handleImage(list);
		}
		++myNextInlineImageInfoIndex;
	}
}

void OleStreamParser::processFloatImage(OleMainStream &stream) {
	const OleMainStream::FloatImageInfoList &imageInfoList = stream.getFloatImageInfoList();
	if (imageInfoList.empty()) {
		return;
	}
	//seek to curCharPos, because not all entries are real pictures
	while(myNextFloatImageInfoIndex < imageInfoList.size() && imageInfoList.at(myNextFloatImageInfoIndex).first < myCurCharPos) {
		++myNextFloatImageInfoIndex;
	}
	while (myNextFloatImageInfoIndex < imageInfoList.size() && imageInfoList.at(myNextFloatImageInfoIndex).first == myCurCharPos) {
		OleMainStream::FloatImageInfo info = imageInfoList.at(myNextFloatImageInfoIndex).second;
		ZLFileImage::Blocks list = stream.getFloatImage(info.ShapeId);
		if (!list.empty()) {
			handleImage(list);
		}
		++myNextFloatImageInfoIndex;
	}
}

void OleStreamParser::processStyles(OleMainStream &stream) {
	const OleMainStream::StyleInfoList &styleInfoList = stream.getStyleInfoList();
	if (!styleInfoList.empty()) {
		while (myNextStyleInfoIndex < styleInfoList.size() && styleInfoList.at(myNextStyleInfoIndex).first == myCurCharPos) {
			OleMainStream::Style info = styleInfoList.at(myNextStyleInfoIndex).second;
			handleParagraphStyle(info);
			++myNextStyleInfoIndex;
		}
	}

	const OleMainStream::CharInfoList &charInfoList = stream.getCharInfoList();
	if (!charInfoList.empty()) {
		while (myNextCharInfoIndex < charInfoList.size() && charInfoList.at(myNextCharInfoIndex).first == myCurCharPos) {
			OleMainStream::CharInfo info = charInfoList.at(myNextCharInfoIndex).second;
			handleFontStyle(info.FontStyle);
			++myNextCharInfoIndex;
		}
	}

	const OleMainStream::BookmarksList &bookmarksList = stream.getBookmarks();
	if (!bookmarksList.empty()) {
		while (myNextBookmarkIndex < bookmarksList.size() && bookmarksList.at(myNextBookmarkIndex).CharPosition == myCurCharPos) {
			OleMainStream::Bookmark bookmark = bookmarksList.at(myNextBookmarkIndex);
			handleBookmark(bookmark.Name);
			++myNextBookmarkIndex;
		}
	}
}
