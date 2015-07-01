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

#ifndef __OLEMAINSTREAM_H__
#define __OLEMAINSTREAM_H__

#include <vector>
#include <string>

#include "OleStream.h"
#include "DocFloatImageReader.h"

class OleMainStream : public OleStream {

public:
	struct Piece {
		enum PieceType {
			PIECE_TEXT,
			PIECE_FOOTNOTE,
			PIECE_OTHER
		};

		int Offset; // TODO: maybe make it unsigned int
		int Length; // TODO: maybe make it unsigned int
		bool IsANSI;
		PieceType Type;
		unsigned int startCP;
	};
	typedef std::vector<Piece> Pieces;

	struct CharInfo {
		enum Font {
			FONT_REGULAR        = 0,
			FONT_BOLD           = 1 << 0,
			FONT_ITALIC         = 1 << 1,
			FONT_UNDERLINE      = 1 << 2,
			FONT_CAPITALS       = 1 << 3,
			FONT_SMALL_CAPS     = 1 << 4,
			FONT_STRIKE         = 1 << 5,
			FONT_HIDDEN         = 1 << 6,
			FONT_MARKDEL        = 1 << 7,
			FONT_SUPERSCRIPT    = 1 << 8,
			FONT_SUBSCRIPT      = 1 << 9
		};

		unsigned int FontStyle;
		unsigned int FontSize;

		CharInfo();
	};
	typedef std::pair<unsigned int, CharInfo> CharPosToCharInfo;
	typedef std::vector<CharPosToCharInfo > CharInfoList;

	struct Style {
		enum AlignmentType {
			ALIGNMENT_LEFT     = 0x00,
			ALIGNMENT_CENTER   = 0x01,
			ALIGNMENT_RIGHT    = 0x02,
			ALIGNMENT_JUSTIFY  = 0x03,
			ALIGNMENT_DEFAULT // for case if alignment is not setted by word
		};

		// style Ids:
		// (this is not full list of possible style ids, enum is used for using in switch-case)
		enum StyleID {
			STYLE_H1       = 0x1,
			STYLE_H2       = 0x2,
			STYLE_H3       = 0x3,
			STYLE_USER     = 0xFFE,
			STYLE_NIL      = 0xFFF,
			STYLE_INVALID  = 0xFFFF
		};

		unsigned int StyleIdCurrent;
		unsigned int StyleIdNext;        // Next style unless overruled

		bool HasPageBreakBefore;
		unsigned int BeforeParagraphIndent;  // Vertical indent before paragraph, pixels
		unsigned int AfterParagraphIndent;   // Vertical indent after paragraph, pixels
		int	LeftIndent;
		int	FirstLineIndent;
		int	RightIndent;
		AlignmentType Alignment;
		CharInfo CurrentCharInfo;

		Style();
	};

	typedef std::pair<unsigned int, Style> CharPosToStyle;
	typedef std::vector<CharPosToStyle> StyleInfoList;
	typedef std::vector<Style> StyleSheet;

	struct SectionInfo {
		unsigned int CharPosition;
		bool IsNewPage;

		SectionInfo();
	};
	typedef std::vector<SectionInfo> SectionInfoList;

	struct Bookmark {
		unsigned int CharPosition;
		std::string Name;
	};
	typedef std::vector<Bookmark> BookmarksList;

	struct InlineImageInfo {
			unsigned int DataPosition;

			InlineImageInfo();
	};
	typedef std::pair<unsigned int, InlineImageInfo> CharPosToInlineImageInfo;
	typedef std::vector<CharPosToInlineImageInfo> InlineImageInfoList;

	struct FloatImageInfo {
			unsigned int ShapeId;
			FloatImageInfo();
	};
	typedef std::pair<unsigned int, FloatImageInfo> CharPosToFloatImageInfo;
	typedef std::vector<CharPosToFloatImageInfo> FloatImageInfoList;

	enum ImageType { //see p. 60 [MS-ODRAW]
		IMAGE_EMF   = 0xF01A,
		IMAGE_WMF   = 0xF01B,
		IMAGE_PICT  = 0xF01C,
		IMAGE_JPEG  = 0xF01D,
		IMAGE_PNG   = 0xF01E,
		IMAGE_DIB   = 0xF01F,
		IMAGE_TIFF  = 0xF029,
		IMAGE_JPEG2 = 0xF02A
	};

public:
	OleMainStream(shared_ptr<OleStorage> storage, OleEntry oleEntry, shared_ptr<ZLInputStream> stream);

public:
	bool open(bool doReadFormattingData);
	const Pieces &getPieces() const;
	const CharInfoList &getCharInfoList() const;
	const StyleInfoList &getStyleInfoList() const;
	const BookmarksList &getBookmarks() const;
	const InlineImageInfoList &getInlineImageInfoList() const;
	const FloatImageInfoList &getFloatImageInfoList() const;

	ZLFileImage::Blocks getFloatImage(unsigned int shapeId) const;
	ZLFileImage::Blocks getInlineImage(unsigned int dataPos) const;

private:
	bool readFIB(const char *headerBuffer);
	bool readPieceTable(const char *headerBuffer, const OleEntry &tableEntry);
	bool readBookmarks(const char *headerBuffer, const OleEntry &tableEntry);
	bool readStylesheet(const char *headerBuffer, const OleEntry &tableEntry);
	bool readSectionsInfoTable(const char *headerBuffer, const OleEntry &tableEntry);
	bool readParagraphStyleTable(const char *headerBuffer, const OleEntry &tableEntry);
	bool readCharInfoTable(const char *headerBuffer, const OleEntry &tableEntry);
	bool readFloatingImages(const char *headerBuffer, const OleEntry &tableEntry);

private: //readPieceTable helpers methods
	static std::string getPiecesTableBuffer(const char *headerBuffer, OleStream &tableStream);
	static void splitPieces(const Pieces &source, Pieces &dest1, Pieces &dest2, Piece::PieceType type1, Piece::PieceType type2, int boundary);

private: //formatting reader helpers methods
	static unsigned int getPrlLength(const char *grpprlBuffer, unsigned int byteNumber);
	static void getCharInfo(unsigned int chpxOffset, unsigned int styleId, const char *grpprlBuffer, unsigned int bytes, CharInfo &charInfo);
	static void getStyleInfo(unsigned int papxOffset, const char *grpprlBuffer, unsigned int bytes, Style &styleInfo);
	static void getSectionInfo(const char *grpprlBuffer, std::size_t bytes, SectionInfo &sectionInfo);
	static bool getInlineImageInfo(unsigned int chpxOffset, const char *grpprlBuffer, unsigned int bytes, InlineImageInfo &pictureInfo);

	static Style getStyleFromStylesheet(unsigned int styleId, const StyleSheet &stylesheet);
	static int getStyleIndex(unsigned int styleId, const std::vector<bool> &isFilled, const StyleSheet &stylesheet);
	static unsigned int getStyleIdByCharPos(unsigned int offset, const StyleInfoList &styleInfoList);

	static bool offsetToCharPos(unsigned int offset, unsigned int &charPos, const Pieces &pieces);
	static bool readToBuffer(std::string &result, unsigned int offset, std::size_t length, OleStream &stream);

	static unsigned int calcCountOfPLC(unsigned int totalSize, unsigned int elementSize);

private:
	enum PrlFlag {
		UNSET = 0,
		SET = 1,
		UNCHANGED = 128,
		NEGATION = 129
	};

private:
	int myStartOfText;
	int myEndOfText;

	Pieces myPieces;

	StyleSheet myStyleSheet;

	CharInfoList myCharInfoList;
	StyleInfoList myStyleInfoList;
	SectionInfoList mySectionInfoList;
	InlineImageInfoList myInlineImageInfoList;
	FloatImageInfoList myFloatImageInfoList;

	BookmarksList myBookmarks;

	shared_ptr<OleStream> myDataStream;

	shared_ptr<DocFloatImageReader> myFLoatImageReader;
};

#endif /* __OLEMAINSTREAM_H__ */
