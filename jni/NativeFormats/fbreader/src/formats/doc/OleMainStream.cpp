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

#include <string>

#include <ZLLogger.h>
#include <ZLUnicodeUtil.h>

#include "OleUtil.h"
#include "OleStorage.h"

#include "DocInlineImageReader.h"

#include "OleMainStream.h"

OleMainStream::Style::Style() :
	StyleIdCurrent(STYLE_INVALID),
	StyleIdNext(STYLE_INVALID),
	HasPageBreakBefore(false),
	BeforeParagraphIndent(0),
	AfterParagraphIndent(0),
	LeftIndent(0),
	FirstLineIndent(0),
	RightIndent(0),
	Alignment(ALIGNMENT_DEFAULT) {
}

OleMainStream::CharInfo::CharInfo() : FontStyle(FONT_REGULAR), FontSize(20) {
}

OleMainStream::SectionInfo::SectionInfo() : CharPosition(0), IsNewPage(true) {
}

OleMainStream::InlineImageInfo::InlineImageInfo() : DataPosition(0) {
}

OleMainStream::FloatImageInfo::FloatImageInfo() : ShapeId(0) {
}

OleMainStream::OleMainStream(shared_ptr<OleStorage> storage, OleEntry oleEntry, shared_ptr<ZLInputStream> stream) : OleStream(storage, oleEntry, stream) {
}

bool OleMainStream::open(bool doReadFormattingData) {
	if (OleStream::open() == false) {
		return false;
	}

	static const std::size_t HEADER_SIZE = 768; //size of data in header of main stream
	char headerBuffer[HEADER_SIZE];
	seek(0, true);

	if (read(headerBuffer, HEADER_SIZE) != HEADER_SIZE) {
		return false;
	}

	bool result = readFIB(headerBuffer);
	if (!result) {
		return false;
	}

	// determining table stream number
	unsigned int tableNumber = (OleUtil::getU2Bytes(headerBuffer, 0xA) & 0x0200) ? 1 : 0;
	std::string tableName = tableNumber == 0 ? "0" : "1";
	tableName += "Table";
	OleEntry tableEntry;
	result = myStorage->getEntryByName(tableName, tableEntry);

	if (!result) {
		// cant't find table stream (that can be only in case if file format is below Word 7/8), so building simple table stream
		// TODO: CHECK may be not all old documents have ANSI
		ZLLogger::Instance().println("DocPlugin", "cant't find table stream, building own simple piece table, that includes all charachters");
		Piece piece = {myStartOfText, myEndOfText - myStartOfText, true, Piece::PIECE_TEXT, 0};
		myPieces.push_back(piece);
		return true;
	}

	result = readPieceTable(headerBuffer, tableEntry);

	if (!result) {
		ZLLogger::Instance().println("DocPlugin", "error during reading piece table");
		return false;
	}

	if (!doReadFormattingData) {
		return true;
	}

	OleEntry dataEntry;
	if (myStorage->getEntryByName("Data", dataEntry)) {
		myDataStream = new OleStream(myStorage, dataEntry, myBaseStream);
	}

	//result of reading following structures doesn't check, because all these
	//problems can be ignored, and document can be showed anyway, maybe with wrong formatting
	readBookmarks(headerBuffer, tableEntry);
	readStylesheet(headerBuffer, tableEntry);
	//readSectionsInfoTable(headerBuffer, tableEntry); //it isn't used now
	readParagraphStyleTable(headerBuffer, tableEntry);
	readCharInfoTable(headerBuffer, tableEntry);
	readFloatingImages(headerBuffer, tableEntry);
	return true;
}

const OleMainStream::Pieces &OleMainStream::getPieces() const {
	return myPieces;
}

const OleMainStream::CharInfoList &OleMainStream::getCharInfoList() const {
	return myCharInfoList;
}

const OleMainStream::StyleInfoList &OleMainStream::getStyleInfoList() const {
	return myStyleInfoList;
}

const OleMainStream::BookmarksList &OleMainStream::getBookmarks() const {
	return myBookmarks;
}

const OleMainStream::InlineImageInfoList &OleMainStream::getInlineImageInfoList() const {
	return myInlineImageInfoList;
}

const OleMainStream::FloatImageInfoList &OleMainStream::getFloatImageInfoList() const {
	return myFloatImageInfoList;
}

ZLFileImage::Blocks OleMainStream::getFloatImage(unsigned int shapeId) const {
	if (myFLoatImageReader.isNull()) {
		return ZLFileImage::Blocks();
	}
	return myFLoatImageReader->getBlocksForShapeId(shapeId);
}

ZLFileImage::Blocks OleMainStream::getInlineImage(unsigned int dataPosition) const {
	if (myDataStream.isNull()) {
		return ZLFileImage::Blocks();
	}
	DocInlineImageReader imageReader(myDataStream);
	return imageReader.getImagePieceInfo(dataPosition);
}

bool OleMainStream::readFIB(const char *headerBuffer) {
	int flags = OleUtil::getU2Bytes(headerBuffer, 0xA); //offset for flags

	if (flags & 0x0004) { //flag for complex format
		ZLLogger::Instance().println("DocPlugin", "This was fast-saved. Some information is lost");
		//lostInfo = (flags & 0xF0) >> 4);
	}

	if (flags & 0x1000) { //flag for using extending charset
		ZLLogger::Instance().println("DocPlugin", "File uses extended character set (get_word8_char)");
	} else {
		ZLLogger::Instance().println("DocPlugin", "File uses get_8bit_char character set");
	}

	if (flags & 0x100) { //flag for encrypted files
		ZLLogger::Instance().println("DocPlugin", "File is encrypted");
		// Encryption key = %08lx ; NumUtil::get4Bytes(header, 14)
		return false;
	}

	unsigned int charset = OleUtil::getU2Bytes(headerBuffer, 0x14); //offset for charset number
	if (charset && charset != 0x100) { //0x100 = default charset
		ZLLogger::Instance().println("DocPlugin", "Using not default character set %d");
	} else {
		ZLLogger::Instance().println("DocPlugin", "Using default character set");
	}

	myStartOfText = OleUtil::get4Bytes(headerBuffer, 0x18); //offset for start of text value
	myEndOfText = OleUtil::get4Bytes(headerBuffer, 0x1c); //offset for end of text value
	return true;
}

void OleMainStream::splitPieces(const Pieces &s, Pieces &dest1, Pieces &dest2, Piece::PieceType type1, Piece::PieceType type2, int boundary) {
	Pieces source = s;
	dest1.clear();
	dest2.clear();

	int sumLength = 0;
	std::size_t i = 0;
	for (i = 0; i < source.size(); ++i) {
		Piece piece = source.at(i);
		if (piece.Length + sumLength >= boundary) {
			Piece piece2 = piece;

			piece.Length = boundary - sumLength;
			piece.Type = type1;

			piece2.Type = type2;
			piece2.Offset += piece.Length * 2;
			piece2.Length -= piece.Length;

			if (piece.Length > 0) {
				dest1.push_back(piece);
			}
			if (piece2.Length > 0) {
				dest2.push_back(piece2);
			}
			++i;
			break;
		}
		sumLength += piece.Length;
		piece.Type = type1;
		dest1.push_back(piece);
	}
	for (; i < source.size(); ++i) {
		Piece piece = source.at(i);
		piece.Type = type2;
		dest2.push_back(piece);
	}

}

std::string OleMainStream::getPiecesTableBuffer(const char *headerBuffer, OleStream &tableStream) {
	unsigned int clxOffset = OleUtil::getU4Bytes(headerBuffer, 0x01A2); //offset for CLX structure
	unsigned int clxLength = OleUtil::getU4Bytes(headerBuffer, 0x01A6); //offset for value of CLX structure length

	//1 step : loading CLX table from table stream
	char *clxBuffer = new char[clxLength];
	if (!tableStream.seek(clxOffset, true)) {
		ZLLogger::Instance().println("DocPlugin", "getPiecesTableBuffer -- error for seeking to CLX structure");
		return std::string();
	}
	if (tableStream.read(clxBuffer, clxLength) != clxLength) {
		ZLLogger::Instance().println("DocPlugin", "getPiecesTableBuffer -- CLX structure length is invalid");
		return std::string();
	}
	std::string clx(clxBuffer, clxLength);
	delete[] clxBuffer;

	//2 step: searching for pieces table buffer at CLX
	//(determines it by 0x02 as start symbol)
	std::size_t from = 0;
	std::size_t i;
	std::string pieceTableBuffer;
	while ((i = clx.find_first_of(0x02, from)) != std::string::npos) {
		if (clx.size() < i + 1 + 4) {
			ZLLogger::Instance().println("DocPlugin", "getPiecesTableBuffer -- CLX structure has invalid format");
			return std::string();
		}
		unsigned int pieceTableLength = OleUtil::getU4Bytes(clx.c_str(), i + 1);
		pieceTableBuffer = std::string(clx, i + 1 + 4);
		if (pieceTableBuffer.length() != pieceTableLength) {
			from = i + 1;
			continue;
		}
		break;
	}
	return pieceTableBuffer;
}


bool OleMainStream::readPieceTable(const char *headerBuffer, const OleEntry &tableEntry) {
	OleStream tableStream(myStorage, tableEntry, myBaseStream);
	std::string piecesTableBuffer = getPiecesTableBuffer(headerBuffer, tableStream);

	if (piecesTableBuffer.empty()) {
		return false;
	}

	//getting count of Character Positions for different types of subdocuments in Main Stream
	int ccpText = OleUtil::get4Bytes(headerBuffer, 0x004C); //text
	int ccpFtn = OleUtil::get4Bytes(headerBuffer, 0x0050); //footnote subdocument
	int ccpHdd = OleUtil::get4Bytes(headerBuffer, 0x0054); //header subdocument
	int ccpMcr = OleUtil::get4Bytes(headerBuffer, 0x0058); //macro subdocument
	int ccpAtn = OleUtil::get4Bytes(headerBuffer, 0x005C); //comment subdocument
	int ccpEdn = OleUtil::get4Bytes(headerBuffer, 0x0060); //endnote subdocument
	int ccpTxbx = OleUtil::get4Bytes(headerBuffer, 0x0064); //textbox subdocument
	int ccpHdrTxbx = OleUtil::get4Bytes(headerBuffer, 0x0068); //textbox subdocument of the header
	int lastCP = ccpFtn + ccpHdd + ccpMcr + ccpAtn + ccpEdn + ccpTxbx + ccpHdrTxbx;
	if (lastCP != 0) {
		++lastCP;
	}
	lastCP += ccpText;

	//getting the CP (character positions) and CP descriptors
	std::vector<int> cp; //array of character positions for pieces
	unsigned int j = 0;
	for (j = 0; ; j += 4) {
		if (piecesTableBuffer.size() < j + 4) {
			ZLLogger::Instance().println("DocPlugin", "invalid piece table, cp ends not with a lastcp");
			break;
		}
		int curCP = OleUtil::get4Bytes(piecesTableBuffer.c_str(), j);
		cp.push_back(curCP);
		if (curCP == lastCP) {
			break;
		}
	}

	if (cp.size() < 2) {
		ZLLogger::Instance().println("DocPlugin", "invalid piece table, < 2 pieces");
		return false;
	}

	std::vector<std::string> descriptors;
	for (std::size_t k = 0; k < cp.size() - 1; ++k) {
		//j + 4, because it should be taken after CP in PiecesTable Buffer
		//k * 8, because it should be taken 8 byte for each descriptor
		std::size_t substrFrom = j + 4 + k * 8;
		if (piecesTableBuffer.size() < substrFrom + 8) {
			ZLLogger::Instance().println("DocPlugin", "invalid piece table, problems with descriptors reading");
			break;
		}
		descriptors.push_back(piecesTableBuffer.substr(substrFrom, 8));
	}

	//filling the Pieces vector
	std::size_t minValidSize = std::min(cp.size() - 1, descriptors.size());
	if (minValidSize == 0) {
		ZLLogger::Instance().println("DocPlugin", "invalid piece table, there are no pieces");
		return false;
	}

	for (std::size_t i = 0; i < minValidSize; ++i) {
		//4byte integer with offset and ANSI flag
		int fcValue = OleUtil::get4Bytes(descriptors.at(i).c_str(), 0x2); //offset for piece structure
		Piece piece;
		piece.IsANSI = (fcValue & 0x40000000) == 0x40000000; //ansi flag
		piece.Offset = fcValue & 0x3FFFFFFF; //gettting offset for current piece
		piece.Length = cp.at(i + 1) - cp.at(i);
		myPieces.push_back(piece);
	}

	//split pieces into different types
	Pieces piecesText, piecesFootnote, piecesOther;
	splitPieces(myPieces, piecesText, piecesFootnote, Piece::PIECE_TEXT, Piece::PIECE_FOOTNOTE, ccpText);
	splitPieces(piecesFootnote, piecesFootnote, piecesOther, Piece::PIECE_FOOTNOTE, Piece::PIECE_OTHER, ccpFtn);

	myPieces.clear();
	for (std::size_t i = 0; i < piecesText.size(); ++i) {
		myPieces.push_back(piecesText.at(i));
	}
	for (std::size_t i = 0; i < piecesFootnote.size(); ++i) {
		myPieces.push_back(piecesFootnote.at(i));
	}
	for (std::size_t i = 0; i < piecesOther.size(); ++i) {
		myPieces.push_back(piecesOther.at(i));
	}

	//converting length and offset depending on isANSI
	for (std::size_t i = 0; i < myPieces.size(); ++i) {
		Piece &piece = myPieces.at(i);
		if (!piece.IsANSI) {
			piece.Length *= 2;
		} else {
			piece.Offset /= 2;
		}
	}

	//filling startCP field
	unsigned int curStartCP = 0;
	for (std::size_t i = 0; i < myPieces.size(); ++i) {
		Piece &piece = myPieces.at(i);
		piece.startCP = curStartCP;
		if (piece.IsANSI) {
			curStartCP += piece.Length;
		} else {
			curStartCP += piece.Length / 2;
		}
	}
	return true;
}

bool OleMainStream::readBookmarks(const char *headerBuffer, const OleEntry &tableEntry) {
	//SttbfBkmk structure is a table of bookmark name strings
	unsigned int beginNamesInfo = OleUtil::getU4Bytes(headerBuffer, 0x142); // address of SttbfBkmk structure
	std::size_t namesInfoLength = (std::size_t)OleUtil::getU4Bytes(headerBuffer, 0x146); // length of SttbfBkmk structure

	if (namesInfoLength == 0) {
		return true; //there's no bookmarks
	}

	OleStream tableStream(myStorage, tableEntry, myBaseStream);
	std::string buffer;
	if (!readToBuffer(buffer, beginNamesInfo, namesInfoLength, tableStream)) {
		return false;
	}

	unsigned int recordsNumber = OleUtil::getU2Bytes(buffer.c_str(), 0x2); //count of records

	std::vector<std::string> names;
	unsigned int offset = 0x6; //initial offset
	for (unsigned int i = 0; i < recordsNumber; ++i) {
		if (buffer.size() < offset + 2) {
			ZLLogger::Instance().println("DocPlugin", "problmes with reading bookmarks names");
			break;
		}
		unsigned int length = OleUtil::getU2Bytes(buffer.c_str(), offset) * 2; //length of string in bytes
		ZLUnicodeUtil::Ucs2String name;
		for (unsigned int j = 0; j < length; j+=2) {
			char ch1 = buffer.at(offset + 2 + j);
			char ch2 = buffer.at(offset + 2 + j + 1);
			ZLUnicodeUtil::Ucs2Char ucs2Char = (unsigned int)ch1 | ((unsigned int)ch2 << 8);
			name.push_back(ucs2Char);
		}
		std::string utf8Name;
		ZLUnicodeUtil::ucs2ToUtf8(utf8Name, name);
		names.push_back(utf8Name);
		offset += length + 2;
	}

	//plcfBkmkf structure is table recording beginning CPs of bookmarks
	unsigned int beginCharPosInfo = OleUtil::getU4Bytes(headerBuffer, 0x14A); // address of plcfBkmkf structure
	std::size_t charPosInfoLen = (std::size_t)OleUtil::getU4Bytes(headerBuffer, 0x14E); // length of plcfBkmkf structure

	if (charPosInfoLen == 0) {
		return true; //there's no bookmarks
	}

	if (!readToBuffer(buffer, beginCharPosInfo, charPosInfoLen, tableStream)) {
		return false;
	}

	static const unsigned int BKF_SIZE = 4;
	std::size_t size = calcCountOfPLC(charPosInfoLen, BKF_SIZE);
	std::vector<unsigned int> charPage;
	for (std::size_t index = 0, offset = 0; index < size; ++index, offset += 4) {
		charPage.push_back(OleUtil::getU4Bytes(buffer.c_str(), offset));
	}

	for (std::size_t i = 0; i < names.size(); ++i) {
		if (i >= charPage.size()) {
			break; //for the case if something in these structures goes wrong, to not to lose all bookmarks
		}
		Bookmark bookmark;
		bookmark.CharPosition = charPage.at(i);
		bookmark.Name = names.at(i);
		myBookmarks.push_back(bookmark);
	}

	return true;
}

bool OleMainStream::readStylesheet(const char *headerBuffer, const OleEntry &tableEntry) {
	//STSH structure is a stylesheet
	unsigned int beginStshInfo = OleUtil::getU4Bytes(headerBuffer, 0xa2); // address of STSH structure
	std::size_t stshInfoLength = (std::size_t)OleUtil::getU4Bytes(headerBuffer, 0xa6); // length of STSH structure

	OleStream tableStream(myStorage, tableEntry, myBaseStream);
	char *buffer = new char[stshInfoLength];
	if (!tableStream.seek(beginStshInfo, true)) {
		ZLLogger::Instance().println("DocPlugin", "problems with reading STSH structure");
		return false;
	}
	if (tableStream.read(buffer, stshInfoLength) != stshInfoLength) {
		ZLLogger::Instance().println("DocPlugin", "problems with reading STSH structure, invalid length");
		return false;
	}

	std::size_t stdCount = (std::size_t)OleUtil::getU2Bytes(buffer, 2);
	std::size_t stdBaseInFile = (std::size_t)OleUtil::getU2Bytes(buffer, 4);
	myStyleSheet.resize(stdCount);

	std::vector<bool> isFilled;
	isFilled.resize(stdCount, false);

	std::size_t stdLen = 0;
	bool styleSheetWasChanged = false;
	do { //make it in while loop, because some base style can be after their successors
		styleSheetWasChanged = false;
		for (std::size_t index = 0, offset = 2 + (std::size_t)OleUtil::getU2Bytes(buffer, 0); index < stdCount; index++, offset += 2 + stdLen) {
			stdLen = (std::size_t)OleUtil::getU2Bytes(buffer, offset);
			if (isFilled.at(index)) {
				continue;
			}

			if (stdLen == 0) {
				//if record is empty, left it default
				isFilled[index] = true;
				continue;
			}

			Style styleInfo = myStyleSheet.at(index);

			const unsigned int styleAndBaseType = OleUtil::getU2Bytes(buffer, offset + 4);
			const unsigned int styleType = styleAndBaseType % 16;
			const unsigned int baseStyleId = styleAndBaseType / 16;
			if (baseStyleId == Style::STYLE_NIL || baseStyleId == Style::STYLE_USER) {
				//if based on nil or user style, left default
			} else {
				int baseStyleIndex = getStyleIndex(baseStyleId, isFilled, myStyleSheet);
				if (baseStyleIndex < 0) {
					//this base style is not filled yet, so pass it at some time
					continue;
				}
				styleInfo = myStyleSheet.at(baseStyleIndex);
				styleInfo.StyleIdCurrent = Style::STYLE_INVALID;
			}

			// parse STD structure
			unsigned int tmp = OleUtil::getU2Bytes(buffer, offset + 6);
			unsigned int upxCount = tmp % 16;
			styleInfo.StyleIdNext = tmp / 16;

			//adding current style
			myStyleSheet[index] = styleInfo;
			isFilled[index] = true;
			styleSheetWasChanged = true;

			std::size_t pos = 2 + stdBaseInFile;
			std::size_t nameLen = (std::size_t)OleUtil::getU2Bytes(buffer, offset + pos);
			nameLen = nameLen * 2 + 2; //from Unicode characters to bytes + Unicode null charachter length
			pos += 2 + nameLen;
			if (pos % 2 != 0) {
				++pos;
			}
			if (pos >= stdLen) {
				continue;
			}
			std::size_t upxLen = (std::size_t)OleUtil::getU2Bytes(buffer, offset + pos);
			if (pos + upxLen > stdLen) {
				//UPX length too large
				continue;
			}
			//for style info styleType must be equal 1
			if (styleType == 1 && upxCount >= 1) {
				if (upxLen >= 2) {
					styleInfo.StyleIdCurrent = OleUtil::getU2Bytes(buffer, offset + pos + 2);
					getStyleInfo(0, buffer + offset + pos + 4, upxLen - 2, styleInfo);
					myStyleSheet[index] = styleInfo;
				}
				pos += 2 + upxLen;
				if (pos % 2 != 0) {
					++pos;
				}
				upxLen = (std::size_t)OleUtil::getU2Bytes(buffer, offset + pos);
			}
			if (upxLen == 0 || pos + upxLen > stdLen) {
				//too small/too large
				continue;
			}
			//for char info styleType can be equal 1 or 2
			if ((styleType == 1 && upxCount >= 2) || (styleType == 2 && upxCount >= 1)) {
				CharInfo charInfo;
				getCharInfo(0, Style::STYLE_INVALID, buffer + offset + pos + 2, upxLen, charInfo);
				styleInfo.CurrentCharInfo = charInfo;
				myStyleSheet[index] = styleInfo;
			}
		}
	} while (styleSheetWasChanged);
	delete[] buffer;
	return true;
}

bool OleMainStream::readCharInfoTable(const char *headerBuffer, const OleEntry &tableEntry) {
	//PlcfbteChpx structure is table with formatting for particular run of text
	unsigned int beginCharInfo = OleUtil::getU4Bytes(headerBuffer, 0xfa); // address of PlcfbteChpx structure
	std::size_t charInfoLength = (std::size_t)OleUtil::getU4Bytes(headerBuffer, 0xfe); // length of PlcfbteChpx structure
	if (charInfoLength < 4) {
		return false;
	}

	OleStream tableStream(myStorage, tableEntry, myBaseStream);
	std::string buffer;
	if (!readToBuffer(buffer, beginCharInfo, charInfoLength, tableStream)) {
		return false;
	}

	static const unsigned int CHPX_SIZE = 4;
	std::size_t size = calcCountOfPLC(charInfoLength, CHPX_SIZE);
	std::vector<unsigned int> charBlocks;
	for (std::size_t index = 0, offset = (size + 1) * 4; index < size; ++index, offset += CHPX_SIZE) {
		charBlocks.push_back(OleUtil::getU4Bytes(buffer.c_str(), offset));
	}

	char *formatPageBuffer = new char[OleStorage::BBD_BLOCK_SIZE];
	for (std::size_t index = 0; index < charBlocks.size(); ++index) {
		seek(charBlocks.at(index) * OleStorage::BBD_BLOCK_SIZE, true);
		if (read(formatPageBuffer, OleStorage::BBD_BLOCK_SIZE) != OleStorage::BBD_BLOCK_SIZE) {
			return false;
		}
		unsigned int crun = OleUtil::getU1Byte(formatPageBuffer, 0x1ff); //offset with crun (count of 'run of text')
		for (unsigned int index2 = 0; index2 < crun; ++index2) {
			unsigned int offset = OleUtil::getU4Bytes(formatPageBuffer, index2 * 4);
			unsigned int chpxOffset = 2 * OleUtil::getU1Byte(formatPageBuffer, (crun + 1) * 4 + index2);
			unsigned int len = OleUtil::getU1Byte(formatPageBuffer, chpxOffset);
			unsigned int charPos = 0;
			if (!offsetToCharPos(offset, charPos, myPieces)) {
				continue;
			}
			unsigned int styleId = getStyleIdByCharPos(charPos, myStyleInfoList);

			CharInfo charInfo = getStyleFromStylesheet(styleId, myStyleSheet).CurrentCharInfo;
			if (chpxOffset != 0) {
				getCharInfo(chpxOffset, styleId, formatPageBuffer + 1, len - 1, charInfo);
			}
			myCharInfoList.push_back(CharPosToCharInfo(charPos, charInfo));

			if (chpxOffset != 0) {
				InlineImageInfo pictureInfo;
				if (getInlineImageInfo(chpxOffset, formatPageBuffer + 1, len - 1, pictureInfo)) {
					myInlineImageInfoList.push_back(CharPosToInlineImageInfo(charPos, pictureInfo));
				}
			}

		}
	}
	delete[] formatPageBuffer;
	return true;
}

bool OleMainStream::readFloatingImages(const char *headerBuffer, const OleEntry &tableEntry) {
	//Plcspa structure is a table with information for FSPA (File Shape Address)
	unsigned int beginPicturesInfo = OleUtil::getU4Bytes(headerBuffer, 0x01DA); // address of Plcspa structure
	if (beginPicturesInfo == 0) {
		return true; //there's no office art objects
	}
	unsigned int picturesInfoLength = OleUtil::getU4Bytes(headerBuffer, 0x01DE); // length of Plcspa structure
	if (picturesInfoLength < 4) {
		return false;
	}

	OleStream tableStream(myStorage, tableEntry, myBaseStream);
	std::string buffer;
	if (!readToBuffer(buffer, beginPicturesInfo, picturesInfoLength, tableStream)) {
		return false;
	}

	static const unsigned int SPA_SIZE = 26;
	std::size_t size = calcCountOfPLC(picturesInfoLength, SPA_SIZE);

	std::vector<unsigned int> picturesBlocks;
	for (std::size_t index = 0, tOffset = 0; index < size; ++index, tOffset += 4) {
		picturesBlocks.push_back(OleUtil::getU4Bytes(buffer.c_str(), tOffset));
	}

	for (std::size_t index = 0, tOffset = (size + 1) * 4; index < size; ++index, tOffset += SPA_SIZE) {
		unsigned int spid = OleUtil::getU4Bytes(buffer.c_str(), tOffset);
		FloatImageInfo info;
		unsigned int charPos = picturesBlocks.at(index);
		info.ShapeId = spid;
		myFloatImageInfoList.push_back(CharPosToFloatImageInfo(charPos, info));
	}

	//DggInfo structure is office art object table data
	unsigned int beginOfficeArtContent = OleUtil::getU4Bytes(headerBuffer, 0x22A); // address of DggInfo structure
	if (beginOfficeArtContent == 0) {
		return true; //there's no office art objects
	}
	unsigned int officeArtContentLength = OleUtil::getU4Bytes(headerBuffer, 0x022E); // length of DggInfo structure
	if (officeArtContentLength < 4) {
		return false;
	}

	shared_ptr<OleStream> newTableStream = new OleStream(myStorage, tableEntry, myBaseStream);
	shared_ptr<OleStream> newMainStream = new OleStream(myStorage, myOleEntry, myBaseStream);
	if (newTableStream->open() && newMainStream->open()) {
		myFLoatImageReader = new DocFloatImageReader(beginOfficeArtContent, officeArtContentLength, newTableStream, newMainStream);
		myFLoatImageReader->readAll();
	}
	return true;
}

bool OleMainStream::readParagraphStyleTable(const char *headerBuffer, const OleEntry &tableEntry) {
	//PlcBtePapx structure is table with formatting for all paragraphs
	unsigned int beginParagraphInfo = OleUtil::getU4Bytes(headerBuffer, 0x102); // address of PlcBtePapx structure
	std::size_t paragraphInfoLength = (std::size_t)OleUtil::getU4Bytes(headerBuffer, 0x106); // length of PlcBtePapx structure
	if (paragraphInfoLength < 4) {
		return false;
	}

	OleStream tableStream(myStorage, tableEntry, myBaseStream);
	std::string buffer;
	if (!readToBuffer(buffer, beginParagraphInfo, paragraphInfoLength, tableStream)) {
		return false;
	}

	static const unsigned int PAPX_SIZE = 4;
	std::size_t size = calcCountOfPLC(paragraphInfoLength, PAPX_SIZE);

	std::vector<unsigned int> paragraphBlocks;
	for (std::size_t index = 0, tOffset = (size + 1) * 4; index < size; ++index, tOffset += PAPX_SIZE) {
		paragraphBlocks.push_back(OleUtil::getU4Bytes(buffer.c_str(), tOffset));
	}

	char *formatPageBuffer = new char[OleStorage::BBD_BLOCK_SIZE];
	for (std::size_t index = 0; index < paragraphBlocks.size(); ++index) {
		seek(paragraphBlocks.at(index) * OleStorage::BBD_BLOCK_SIZE, true);
		if (read(formatPageBuffer, OleStorage::BBD_BLOCK_SIZE) != OleStorage::BBD_BLOCK_SIZE) {
			return false;
		}
		const unsigned int paragraphsCount = OleUtil::getU1Byte(formatPageBuffer, 0x1ff); //offset with 'cpara' value (count of paragraphs)
		for (unsigned int index2 = 0; index2 < paragraphsCount; ++index2) {
			const unsigned int offset = OleUtil::getU4Bytes(formatPageBuffer, index2 * 4);
			unsigned int papxOffset = OleUtil::getU1Byte(formatPageBuffer, (paragraphsCount + 1) * 4 + index2 * 13) * 2;
			if (papxOffset <= 0) {
				continue;
			}
			unsigned int len = OleUtil::getU1Byte(formatPageBuffer, papxOffset) * 2;
			if (len == 0) {
				++papxOffset;
				len = OleUtil::getU1Byte(formatPageBuffer, papxOffset) * 2;
			}

			const unsigned int styleId = OleUtil::getU2Bytes(formatPageBuffer, papxOffset + 1);
			Style styleInfo = getStyleFromStylesheet(styleId, myStyleSheet);

			if (len >= 3) {
				getStyleInfo(papxOffset, formatPageBuffer + 3, len - 3, styleInfo);
			}

			unsigned int charPos = 0;
			if (!offsetToCharPos(offset, charPos, myPieces)) {
				continue;
			}
			myStyleInfoList.push_back(CharPosToStyle(charPos, styleInfo));
		}
	}
	delete[] formatPageBuffer;
	return true;
}

bool OleMainStream::readSectionsInfoTable(const char *headerBuffer, const OleEntry &tableEntry) {
	//PlcfSed structure is a section table
	unsigned int beginOfText = OleUtil::getU4Bytes(headerBuffer, 0x18); //address of text's begin in main stream
	unsigned int beginSectInfo = OleUtil::getU4Bytes(headerBuffer, 0xca); //address if PlcfSed structure

	std::size_t sectInfoLen = (std::size_t)OleUtil::getU4Bytes(headerBuffer, 0xce); //length of PlcfSed structure
	if (sectInfoLen < 4) {
		return false;
	}

	OleStream tableStream(myStorage, tableEntry, myBaseStream);
	std::string buffer;
	if (!readToBuffer(buffer, beginSectInfo, sectInfoLen, tableStream)) {
		return false;
	}

	static const unsigned int SED_SIZE = 12;
	std::size_t decriptorsCount = calcCountOfPLC(sectInfoLen, SED_SIZE);

	//saving the section offsets (in character positions)
	std::vector<unsigned int> charPos;
	for (std::size_t index = 0, tOffset = 0; index < decriptorsCount; ++index, tOffset += 4) {
		unsigned int ulTextOffset = OleUtil::getU4Bytes(buffer.c_str(), tOffset);
		charPos.push_back(beginOfText + ulTextOffset);
	}

	//saving sepx offsets
	std::vector<unsigned int> sectPage;
	for (std::size_t index = 0, tOffset = (decriptorsCount + 1) * 4; index < decriptorsCount; ++index, tOffset += SED_SIZE) {
		sectPage.push_back(OleUtil::getU4Bytes(buffer.c_str(), tOffset + 2));
	}

	//reading the section properties
	char tmpBuffer[2];
	for (std::size_t index = 0; index < sectPage.size(); ++index) {
		if (sectPage.at(index) == 0xffffffffUL) { //check for invalid record, to make default section info
			SectionInfo sectionInfo;
			sectionInfo.CharPosition = charPos.at(index);
			mySectionInfoList.push_back(sectionInfo);
			continue;
		}
		//getting number of bytes to read
		if (!seek(sectPage.at(index), true)) {
			continue;
		}
		if (read(tmpBuffer, 2) != 2) {
			continue;
		}
		std::size_t bytes = 2 + (std::size_t)OleUtil::getU2Bytes(tmpBuffer, 0);

		if (!seek(sectPage.at(index), true)) {
			continue;
		}
		char *formatPageBuffer = new char[bytes];
		if (read(formatPageBuffer, bytes) != bytes) {
			delete[] formatPageBuffer;
			continue;
		}
		SectionInfo sectionInfo;
		sectionInfo.CharPosition = charPos.at(index);
		getSectionInfo(formatPageBuffer + 2, bytes - 2, sectionInfo);
		mySectionInfoList.push_back(sectionInfo);
		delete[] formatPageBuffer;
	}
	return true;
}

void OleMainStream::getStyleInfo(unsigned int papxOffset, const char *grpprlBuffer, unsigned int bytes, Style &styleInfo) {
	int	tmp, toDelete, toAdd;
	unsigned int offset = 0;
	while (bytes >= offset + 2) {
		unsigned int curPrlLength = 0;
		switch (OleUtil::getU2Bytes(grpprlBuffer, papxOffset + offset)) {
			case 0x2403:
				styleInfo.Alignment = (Style::AlignmentType)OleUtil::getU1Byte(grpprlBuffer, papxOffset + offset + 2);
				break;
			case 0x4610:
				styleInfo.LeftIndent += OleUtil::getU2Bytes(grpprlBuffer, papxOffset + offset + 2);
				if (styleInfo.LeftIndent < 0) {
					styleInfo.LeftIndent = 0;
				}
				break;
			case 0xc60d: // ChgTabsPapx
			case 0xc615: // ChgTabs
				tmp = OleUtil::get1Byte(grpprlBuffer, papxOffset + offset + 2);
				if (tmp < 2) {
					curPrlLength = 1;
					break;
				}
				toDelete = OleUtil::getU1Byte(grpprlBuffer, papxOffset + offset + 3);
				if (tmp < 2 + 2 * toDelete) {
					curPrlLength = 1;
					break;
				}
				toAdd = OleUtil::getU1Byte(grpprlBuffer, papxOffset + offset + 4 + 2 * toDelete);
				if (tmp < 2 + 2 * toDelete + 2 * toAdd) {
					curPrlLength = 1;
					break;
				}
				break;
			case 0x840e:
				styleInfo.RightIndent = (int)OleUtil::getU2Bytes(grpprlBuffer, papxOffset + offset + 2);
				break;
			case 0x840f:
				styleInfo.LeftIndent = (int)OleUtil::getU2Bytes(grpprlBuffer, papxOffset + offset + 2);
				break;
			case 0x8411:
				styleInfo.FirstLineIndent = (int)OleUtil::getU2Bytes(grpprlBuffer, papxOffset + offset + 2);
				break;
			case 0xa413:
				styleInfo.BeforeParagraphIndent = OleUtil::getU2Bytes(grpprlBuffer, papxOffset + offset + 2);
				break;
			case 0xa414:
				styleInfo.AfterParagraphIndent = OleUtil::getU2Bytes(grpprlBuffer, papxOffset + offset + 2);
				break;
			case 0x2407:
				styleInfo.HasPageBreakBefore = OleUtil::getU1Byte(grpprlBuffer, papxOffset + offset + 2) == 0x01;
				break;
			default:
				break;
		}
		if (curPrlLength == 0) {
			curPrlLength = getPrlLength(grpprlBuffer, papxOffset + offset);
		}
		offset += curPrlLength;
	}

}

void OleMainStream::getCharInfo(unsigned int chpxOffset, unsigned int /*styleId*/, const char *grpprlBuffer, unsigned int bytes, CharInfo &charInfo) {
	unsigned int offset = 0;
	while (bytes >= offset + 2) {
		switch (OleUtil::getU2Bytes(grpprlBuffer, chpxOffset + offset)) {
			case 0x0835: //bold
				if (bytes >= offset + 3) {
					switch (OleUtil::getU1Byte(grpprlBuffer, chpxOffset + offset + 2)) {
						case UNSET:
							charInfo.FontStyle &= ~CharInfo::FONT_BOLD;
							break;
						case SET:
							charInfo.FontStyle |= CharInfo::FONT_BOLD;
							break;
						case UNCHANGED:
							break;
						case NEGATION:
							charInfo.FontStyle ^= CharInfo::FONT_BOLD;
							break;
						default:
							break;
					}
				}
				break;
			case 0x0836: //italic
				if (bytes >= offset + 3) {
					switch (OleUtil::getU1Byte(grpprlBuffer, chpxOffset + offset + 2)) {
						case UNSET:
							charInfo.FontStyle &= ~CharInfo::FONT_ITALIC;
							break;
						case SET:
							charInfo.FontStyle |= CharInfo::FONT_ITALIC;
							break;
						case UNCHANGED:
							break;
						case NEGATION:
							charInfo.FontStyle ^= CharInfo::FONT_ITALIC;
							break;
						default:
							break;
					}
				}
				break;
			case 0x4a43: //size of font
				if (bytes >= offset + 4) {
					charInfo.FontSize = OleUtil::getU2Bytes(grpprlBuffer, chpxOffset + offset + 2);
				}
				break;
			default:
				break;
		}
		offset += getPrlLength(grpprlBuffer, chpxOffset + offset);
	}
}

void OleMainStream::getSectionInfo(const char *grpprlBuffer, std::size_t bytes, SectionInfo &sectionInfo) {
	unsigned int tmp;
	std::size_t offset = 0;
	while (bytes >= offset + 2) {
		switch (OleUtil::getU2Bytes(grpprlBuffer, offset)) {
			case 0x3009: //new page
				tmp = OleUtil::getU1Byte(grpprlBuffer, offset + 2);
				sectionInfo.IsNewPage = (tmp != 0 && tmp != 1);
				break;
			default:
				break;
		}
		offset += getPrlLength(grpprlBuffer, offset);
	}
}

bool OleMainStream::getInlineImageInfo(unsigned int chpxOffset, const char *grpprlBuffer, unsigned int bytes, InlineImageInfo &pictureInfo) {
	//p. 105 of [MS-DOC] documentation
	unsigned int offset = 0;
	bool isFound = false;
	while (bytes >= offset + 2) {
		switch (OleUtil::getU2Bytes(grpprlBuffer, chpxOffset + offset)) {
			case 0x080a: // ole object, p.107 [MS-DOC]
				if (OleUtil::getU1Byte(grpprlBuffer, chpxOffset + offset + 2) == 0x01) {
					return false;
				}
				break;
			case 0x0806: // is not a picture, but a binary data? (sprmCFData, p.106 [MS-DOC])
				if (OleUtil::getU4Bytes(grpprlBuffer, chpxOffset + offset + 2) == 0x01) {
					return false;
				}
				break;
//			case 0x0855: // sprmCFSpec, p.117 [MS-DOC], MUST BE applied with a value of 1 (see p.105 [MS-DOC])
//				if (OleUtil::getU1Byte(grpprlBuffer, chpxOffset + offset + 2) != 0x01) {
//					return false;
//				}
//				break;
			case 0x6a03: // location p.105 [MS-DOC]
				pictureInfo.DataPosition = OleUtil::getU4Bytes(grpprlBuffer, chpxOffset + offset + 2);
				isFound = true;
				break;
			default:
				break;
		}
		offset += getPrlLength(grpprlBuffer, chpxOffset + offset);
	}
	return isFound;
}

OleMainStream::Style OleMainStream::getStyleFromStylesheet(unsigned int styleId, const StyleSheet &stylesheet) {
	//TODO optimize it: StyleSheet can be map structure with styleId key
	Style style;
	if (styleId != Style::STYLE_INVALID && styleId != Style::STYLE_NIL && styleId != Style::STYLE_USER) {
		for (std::size_t index = 0; index < stylesheet.size(); ++index) {
			if (stylesheet.at(index).StyleIdCurrent == styleId) {
				return stylesheet.at(index);
			}
		}
	}
	style.StyleIdCurrent = styleId;
	return style;
}

int OleMainStream::getStyleIndex(unsigned int styleId, const std::vector<bool> &isFilled, const StyleSheet &stylesheet) {
	//TODO optimize it: StyleSheet can be map structure with styleId key
	//in that case, this method will be excess
	if (styleId == Style::STYLE_INVALID) {
		return -1;
	}
	for (int index = 0; index < (int)stylesheet.size(); ++index) {
		if (isFilled.at(index) && stylesheet.at(index).StyleIdCurrent == styleId) {
			return index;
		}
	}
	return -1;
}

unsigned int OleMainStream::getStyleIdByCharPos(unsigned int charPos, const StyleInfoList &styleInfoList) {
	unsigned int styleId = Style::STYLE_INVALID;
	for (std::size_t i = 0; i < styleInfoList.size(); ++i) {
		const Style &info = styleInfoList.at(i).second;
		if (i == styleInfoList.size() - 1) { //if last
			styleId = info.StyleIdCurrent;
			break;
		}
		unsigned int curOffset = styleInfoList.at(i).first;
		unsigned int nextOffset = styleInfoList.at(i + 1).first;
		if (charPos >= curOffset && charPos < nextOffset) {
			styleId = info.StyleIdCurrent;
			break;
		}
	}
	return styleId;
}

bool OleMainStream::offsetToCharPos(unsigned int offset, unsigned int &charPos, const Pieces &pieces) {
	if (pieces.empty()) {
		return false;
	}
	if ((unsigned int)pieces.front().Offset > offset) {
		charPos = 0;
		return true;
	}
	if ((unsigned int)(pieces.back().Offset + pieces.back().Length) <= offset) {
		return false;
	}

	std::size_t pieceNumber = 0;
	for (std::size_t i = 0; i < pieces.size(); ++i) {
		if (i == pieces.size() - 1) { //if last
			pieceNumber = i;
			break;
		}
		unsigned int curOffset = pieces.at(i).Offset;
		unsigned int nextOffset = pieces.at(i + 1).Offset;
		if (offset >= curOffset && offset < nextOffset) {
			pieceNumber = i;
			break;
		}
	}

	const Piece &piece = pieces.at(pieceNumber);
	unsigned int diffOffset = offset - piece.Offset;
	if (!piece.IsANSI) {
		diffOffset /= 2;
	}
	charPos = piece.startCP + diffOffset;
	return true;
}

bool OleMainStream::readToBuffer(std::string &result, unsigned int offset, std::size_t length, OleStream &stream) {
	char *buffer = new char[length];
	stream.seek(offset, true);
	if (stream.read(buffer, length) != length) {
		return false;
	}
	result = std::string(buffer, length);
	delete[] buffer;
	return true;
}

unsigned int OleMainStream::calcCountOfPLC(unsigned int totalSize, unsigned int elementSize) {
	//calculates count of elements in PLC structure, formula from p.30 [MS-DOC]
	return (totalSize - 4) / (4 + elementSize);
}

unsigned int OleMainStream::getPrlLength(const char *grpprlBuffer, unsigned int byteNumber) {
	unsigned int tmp;
	unsigned int opCode = OleUtil::getU2Bytes(grpprlBuffer, byteNumber);
	switch (opCode & 0xE000) {
		case 0x0000:
		case 0x2000:
			return 3;
		case 0x4000:
		case 0x8000:
		case 0xA000:
			return 4;
		case 0xE000:
			return 5;
		case 0x6000:
			return 6;
		case 0xC000:
			//counting of info length
			tmp = OleUtil::getU1Byte(grpprlBuffer, byteNumber + 2);
			if (opCode == 0xC615 && tmp == 255) {
				unsigned int del = OleUtil::getU1Byte(grpprlBuffer, byteNumber + 3);
				unsigned int add = OleUtil::getU1Byte(grpprlBuffer, byteNumber + 4 + del * 4);
				tmp = 2 + del * 4 + add * 3;
			}
			return 3 + tmp;
		default:
			return 1;
	}
}
