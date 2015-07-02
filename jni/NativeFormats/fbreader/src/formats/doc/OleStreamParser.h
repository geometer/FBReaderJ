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

#ifndef __OLESTREAMPARSER_H__
#define __OLESTREAMPARSER_H__

#include <ZLUnicodeUtil.h>

#include "OleMainStream.h"
#include "OleStreamReader.h"

class OleStreamParser : public OleStreamReader {

public:
	//word's control chars:
	static const ZLUnicodeUtil::Ucs2Char WORD_FOOTNOTE_MARK;
	static const ZLUnicodeUtil::Ucs2Char WORD_TABLE_SEPARATOR;
	static const ZLUnicodeUtil::Ucs2Char WORD_HORIZONTAL_TAB;
	static const ZLUnicodeUtil::Ucs2Char WORD_HARD_LINEBREAK;
	static const ZLUnicodeUtil::Ucs2Char WORD_PAGE_BREAK;
	static const ZLUnicodeUtil::Ucs2Char WORD_END_OF_PARAGRAPH;
	static const ZLUnicodeUtil::Ucs2Char WORD_MINUS;
	static const ZLUnicodeUtil::Ucs2Char WORD_SOFT_HYPHEN;
	static const ZLUnicodeUtil::Ucs2Char WORD_START_FIELD;
	static const ZLUnicodeUtil::Ucs2Char WORD_SEPARATOR_FIELD;
	static const ZLUnicodeUtil::Ucs2Char WORD_END_FIELD;
	static const ZLUnicodeUtil::Ucs2Char WORD_ZERO_WIDTH_UNBREAKABLE_SPACE;
	static const ZLUnicodeUtil::Ucs2Char INLINE_IMAGE;
	static const ZLUnicodeUtil::Ucs2Char FLOAT_IMAGE;

	//unicode values:
	static const ZLUnicodeUtil::Ucs2Char NULL_SYMBOL;
	static const ZLUnicodeUtil::Ucs2Char FILE_SEPARATOR;
	static const ZLUnicodeUtil::Ucs2Char LINE_FEED;
	static const ZLUnicodeUtil::Ucs2Char SOFT_HYPHEN;
	static const ZLUnicodeUtil::Ucs2Char SPACE;
	static const ZLUnicodeUtil::Ucs2Char MINUS;
	static const ZLUnicodeUtil::Ucs2Char VERTICAL_LINE;

public:
	OleStreamParser();

private:
	bool readStream(OleMainStream &stream);

protected:
	virtual void handleChar(ZLUnicodeUtil::Ucs2Char ucs2char) = 0;
	virtual void handleHardLinebreak() = 0;
	virtual void handleParagraphEnd() = 0;
	virtual void handlePageBreak() = 0;
	virtual void handleTableSeparator() = 0;
	virtual void handleTableEndRow() = 0;
	virtual void handleFootNoteMark() = 0;
	virtual void handleStartField() = 0;
	virtual void handleSeparatorField() = 0;
	virtual void handleEndField() = 0;
	virtual void handleImage(const ZLFileImage::Blocks &blocks) = 0;
	virtual void handleOtherControlChar(ZLUnicodeUtil::Ucs2Char ucs2char) = 0;

	virtual void handleFontStyle(unsigned int fontStyle) = 0;
	virtual void handleParagraphStyle(const OleMainStream::Style &styleInfo) = 0;
	virtual void handleBookmark(const std::string &name) = 0;

private:
	bool getUcs2Char(OleMainStream &stream, ZLUnicodeUtil::Ucs2Char &ucs2char);
	void processInlineImage(OleMainStream &stream);
	void processFloatImage(OleMainStream &stream);
	void processStyles(OleMainStream &stream);

private:
protected:
	ZLUnicodeUtil::Ucs2String myBuffer;
private:
	std::size_t myCurBufferPosition;

	unsigned int myCurCharPos;

	std::size_t myNextStyleInfoIndex;
	std::size_t myNextCharInfoIndex;
	std::size_t myNextBookmarkIndex;
	std::size_t myNextInlineImageInfoIndex;
	std::size_t myNextFloatImageInfoIndex;
};

#endif /* __OLESTREAMPARSER_H__ */
