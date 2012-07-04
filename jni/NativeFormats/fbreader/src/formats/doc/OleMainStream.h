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

#ifndef __OLEMAINSTREAM_H__
#define __OLEMAINSTREAM_H__

#include <vector>
#include <string>

#include "OleStream.h"

class OleMainStream : public OleStream {
public:
	struct Piece {
		enum PieceType {
			TEXT,
			FOOTNOTE,
			OTHER
		};

		int offset; //maybe make it unsigned int
		int length; //maybe make it unsigned int
		bool isANSI;
		PieceType type;
		unsigned int startCP;
	};
	typedef std::vector<Piece> Pieces;

	struct CharInfo {

		enum Font {
			REGULAR = 0x0000,
			BOLD = 0x0001,
			ITALIC = 0x0002,
			UNDERLINE = 0x0004,
			CAPITALS = 0x0008,
			SMALL_CAPITALS = 0x0010,
			STRIKE = 0x0020,
			HIDDEN = 0x0040,
			MARKDEL = 0x0080,
			SUPERSCRIPT = 0x0100,
			SUBSCRIPT = 0x0200
		};

		unsigned int fontStyle;
		unsigned int fontSize;

		CharInfo();
	};
	typedef std::pair<unsigned int, CharInfo> CharPosToCharInfo;
	typedef std::vector<CharPosToCharInfo > CharInfoList;

	struct Style {

		enum Alignment {
			LEFT = 0x00,
			CENTER = 0x01,
			RIGHT = 0x02,
			JUSTIFY = 0x03
		};

		unsigned int istd; //Current style
		unsigned int istdNext; //Next style unless overruled
		bool hasPageBreakBefore;
		unsigned int beforeIndent; //Vertical indent before paragraph
		unsigned int afterIndent; //Vertical indent after paragraph
		int	leftIndent; //Left indent
		int	firstLineIndent; //First line left indent
		int	rightIndent; //Right indent
		unsigned int alignment;

		CharInfo charInfo;
		Style();
	};
	typedef std::pair<unsigned int, Style> CharPosToStyle;
	typedef std::vector<CharPosToStyle> StyleInfoList;
	typedef std::vector<Style> StyleSheet;

	enum StyleID {
		H1 = 0x1,
		H2 = 0x2,
		H3 = 0x3,
		STI_USER = 0xFFE,
		STI_NIL = 0xFFF,
		ISTD_INVALID = 0xFFFF
	};

	struct SectionInfo {
		unsigned int charPos;
		bool newPage;
		SectionInfo();
	};
	typedef std::vector<SectionInfo> SectionInfoList;

	struct Bookmark {
		unsigned int charPos;
		std::string name;
	};
	typedef std::vector<Bookmark> Bookmarks;

	struct PictureInfo {
			unsigned int dataPos;

			PictureInfo();
	};
	typedef std::pair<unsigned int, PictureInfo> CharPosToPictureInfo;
	typedef std::vector<CharPosToPictureInfo> PictureInfoList;

public:
	OleMainStream(shared_ptr<OleStorage> storage, OleEntry oleEntry, shared_ptr<ZLInputStream> stream);

public:
	bool open();
	const Pieces &getPieces() const;
	const CharInfoList &getCharInfoList() const;
	const StyleInfoList &getStyleInfoList() const;
	const Bookmarks &getBookmarks() const;
	const PictureInfoList &getPictureInfoList() const;
	shared_ptr<OleStream> dataStream() const;

private:
	bool readFIB(const char *headerBuffer);
	bool readPieceTable(const char *headerBuffer, const OleEntry &tableEntry);
	bool readBookmarks(const char *headerBuffer, const OleEntry &tableEntry);
	bool readStylesheet(const char *headerBuffer, const OleEntry &tableEntry);
	bool readSectionsInfoTable(const char *headerBuffer, const OleEntry &tableEntry);
	bool readParagraphStyleTable(const char *headerBuffer, const OleEntry &tableEntry);
	bool readCharInfoTable(const char *headerBuffer, const OleEntry &tableEntry);

private: //readPieceTable helpers methods
	static std::string getPiecesTableBuffer(const char *headerBuffer, OleStream &tableStream);
	static void splitPieces(const Pieces &source, Pieces &dest1, Pieces &dest2, Piece::PieceType type1, Piece::PieceType type2, int boundary);

private: //formatting reader helpers methods
	static unsigned int getPrlLength(const char *grpprlBuffer, unsigned int byteNumber);
	static void getCharInfo(unsigned int chpxOffset, unsigned int istd, const char *grpprlBuffer, unsigned int bytes, CharInfo &charInfo);
	static void getStyleInfo(unsigned int papxOffset, const char *grpprlBuffer, unsigned int bytes, Style &styleInfo);
	static void getSectionInfo(const char *grpprlBuffer, size_t bytes, SectionInfo &sectionInfo);
	static bool getPictureInfo(unsigned int chpxOffset, const char *grpprlBuffer, unsigned int bytes, PictureInfo &pictureInfo);

	static Style getStyleFromStylesheet(unsigned int istd, const StyleSheet &stylesheet);
	static int getStyleIndex(unsigned int istd, const std::vector<bool> &isFilled, const StyleSheet &stylesheet);
	static unsigned int getIstdByCharPos(unsigned int offset, const StyleInfoList &styleInfoList);

	static bool offsetToCharPos(unsigned int offset, unsigned int &charPos, const Pieces &pieces);
	static bool readToBuffer(std::string &result, unsigned int offset, size_t length, OleStream &stream);

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
	PictureInfoList myPictureInfoList;

	Bookmarks myBookmarks;

	shared_ptr<OleStream> myDataStream;
};

#endif /* __OLEMAINSTREAM_H__ */
