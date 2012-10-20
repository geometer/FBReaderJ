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


#include <cctype>
#include <cstring>

#include <ZLLogger.h>

#include "OleMainStream.h"
#include "DocBookReader.h"
#include "OleUtil.h"
#include "DocInlineImageReader.h"

#include "OleStreamReader.h"

//word's control chars:
const ZLUnicodeUtil::Ucs2Char OleStreamReader::WORD_FOOTNOTE_MARK = 0x0002;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::WORD_TABLE_SEPARATOR = 0x0007;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::WORD_HORIZONTAL_TAB = 0x0009;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::WORD_HARD_LINEBREAK = 0x000b;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::WORD_PAGE_BREAK = 0x000c;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::WORD_END_OF_PARAGRAPH = 0x000d;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::WORD_SHORT_DEFIS = 0x001e;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::WORD_SOFT_HYPHEN = 0x001f;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::WORD_START_FIELD = 0x0013;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::WORD_SEPARATOR_FIELD = 0x0014;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::WORD_END_FIELD = 0x0015;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::WORD_ZERO_WIDTH_UNBREAKABLE_SPACE = 0xfeff;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::INLINE_IMAGE = 0x0001;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::FLOAT_IMAGE = 0x0008;

//unicode values:
const ZLUnicodeUtil::Ucs2Char OleStreamReader::NULL_SYMBOL = 0x0;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::FILE_SEPARATOR = 0x1c;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::LINE_FEED = 0x000a;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::SOFT_HYPHEN = 0xad;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::SPACE = 0x20;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::SHORT_DEFIS = 0x2D;
const ZLUnicodeUtil::Ucs2Char OleStreamReader::VERTICAL_LINE = 0x7C;

OleStreamReader::OleStreamReader(const std::string &encoding) :
	myEncoding(encoding) {
	clear();
}

void OleStreamReader::clear() {
	myBuffer.clear();
	myCurBufferPosition = 0;
	myNextPieceNumber = 0;

	myCurCharPos = 0;
	myNextStyleInfoIndex = 0;
	myNextCharInfoIndex = 0;
	myNextBookmarkIndex = 0;
	myNextInlineImageInfoIndex = 0;
	myNextFloatImageInfoIndex = 0;
}

bool OleStreamReader::readStream(OleMainStream &oleMainStream) {
	clear();

	bool res = oleMainStream.open();
	if (!res) {
		ZLLogger::Instance().println("OleStreamReader", "doesn't open correct");
		return false;
	}
	ZLUnicodeUtil::Ucs2Char ucs2char;
	bool tabMode = false;
	while (getUcs2Char(oleMainStream, ucs2char)) {
		if (ucs2char < 32) { //< 32 are control symbols
			//printf("[0x%x]", ucs2char); //debug output
		}

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
					//printf("\n");
					handleHardLinebreak();
					break;
				case WORD_END_OF_PARAGRAPH:
				case WORD_PAGE_BREAK:
					//printf("\n");
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
				case INLINE_IMAGE: case FLOAT_IMAGE:
					break;
				default:
					handleOtherControlChar(ucs2char);
					break;
			}
		} else if (ucs2char == WORD_ZERO_WIDTH_UNBREAKABLE_SPACE) {
			continue; //skip
		} else {
			//debug output
//			std::string utf8String;
//			ZLUnicodeUtil::Ucs2String ucs2String;
//			ucs2String.push_back(ucs2char);
//			ZLUnicodeUtil::ucs2ToUtf8(utf8String, ucs2String);
//			printf("%s", utf8String.c_str());

			handleChar(ucs2char);
		}
	}

	return true;
}

bool OleStreamReader::getUcs2Char(OleMainStream &stream, ZLUnicodeUtil::Ucs2Char &ucs2char) {
	if (myCurBufferPosition >= myBuffer.size()) {
		if (!fillBuffer(stream)) {
			return false;
		}
	}
	ucs2char = myBuffer.at(myCurBufferPosition++);
	processStyles(stream);

	if (ucs2char == INLINE_IMAGE) {
		processInlineImage(stream);
	} else if (ucs2char == FLOAT_IMAGE) {
		processFloatImage(stream);
	}
	++myCurCharPos;
	return true;
}

void OleStreamReader::processInlineImage(OleMainStream &stream) {
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

void OleStreamReader::processFloatImage(OleMainStream &stream) {
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

void OleStreamReader::processStyles(OleMainStream &stream) {
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

bool OleStreamReader::fillBuffer(OleMainStream &stream) {
	const OleMainStream::Pieces &pieces = stream.getPieces();
	if (myNextPieceNumber >= pieces.size()) {
		return false; //end of reading
	}
	const OleMainStream::Piece &piece = pieces.at(myNextPieceNumber);

	if (piece.Type == OleMainStream::Piece::PIECE_FOOTNOTE) {
		handlePageBreak();
	} else if (piece.Type == OleMainStream::Piece::PIECE_OTHER) {
		return false;
	}

	if (!stream.seek(piece.Offset, true)) {
		//TODO maybe in that case we should take next piece?
		return false;
	}
	char *textBuffer = new char[piece.Length];
	size_t readedBytes = stream.read(textBuffer, piece.Length);
	if (readedBytes != (unsigned int)piece.Length) {
		ZLLogger::Instance().println("OleStreamReader", "not all bytes has been readed from piece");
	}

	myBuffer.clear();
	if (!piece.IsANSI) {
		for (unsigned int i = 0; i < readedBytes; i += 2) {
			ZLUnicodeUtil::Ucs2Char ch = OleUtil::getU2Bytes(textBuffer, i);
			myBuffer.push_back(ch);
		}
	} else {
		if (myConverter.isNull()) {
			//lazy convertor loading, because documents can be in Unicode only and don't need to be converted
			ZLEncodingCollection &collection = ZLEncodingCollection::Instance();
			myConverter = collection.converter(myEncoding);
			if (myConverter.isNull()) {
				myConverter = collection.defaultConverter();
			}
		}
		std::string utf8String;
		myConverter->convert(utf8String, std::string(textBuffer, readedBytes));
		ZLUnicodeUtil::utf8ToUcs2(myBuffer, utf8String);
	}
	myCurBufferPosition = 0;
	++myNextPieceNumber;
	delete[] textBuffer;

	return true;
}
