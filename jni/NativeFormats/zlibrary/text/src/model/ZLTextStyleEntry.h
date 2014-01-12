/*
 * Copyright (C) 2004-2014 Geometer Plus <contact@geometerplus.com>
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

#ifndef __ZLTEXTSTYLEENTRY_H__
#define __ZLTEXTSTYLEENTRY_H__

#include <string>

#include <ZLTextParagraph.h>
#include <ZLTextAlignmentType.h>
#include <ZLBoolean3.h>

class ZLTextStyleEntry : public ZLTextParagraphEntry {

public:
	enum SizeUnit {
		SIZE_UNIT_PIXEL,
		SIZE_UNIT_POINT,
		SIZE_UNIT_EM_100,
		SIZE_UNIT_EX_100,
		SIZE_UNIT_PERCENT
	};

	struct Metrics {
		Metrics(int fontSize, int fontXHeight, int fullWidth, int fullHeight);

		int FontSize;
		int FontXHeight;
		int FullWidth;
		int FullHeight;
	};

	enum FontModifier {
		FONT_MODIFIER_BOLD =           1 << 0,
		FONT_MODIFIER_ITALIC =         1 << 1,
		FONT_MODIFIER_UNDERLINED =     1 << 2,
		FONT_MODIFIER_STRIKEDTHROUGH = 1 << 3,
		FONT_MODIFIER_SMALLCAPS =      1 << 4,
		FONT_MODIFIER_INHERIT =        1 << 5,
		FONT_MODIFIER_SMALLER =        1 << 6,
		FONT_MODIFIER_LARGER =         1 << 7,
	};

	enum Feature {
		LENGTH_LEFT_INDENT =                0,
		LENGTH_RIGHT_INDENT =               1,
		LENGTH_FIRST_LINE_INDENT_DELTA =    2,
		LENGTH_SPACE_BEFORE =               3,
		LENGTH_SPACE_AFTER =                4,
		LENGTH_FONT_SIZE =                  5,
		NUMBER_OF_LENGTHS =                 6,
		ALIGNMENT_TYPE =                    NUMBER_OF_LENGTHS,
		FONT_FAMILY =                       NUMBER_OF_LENGTHS + 1,
		FONT_STYLE_MODIFIER =               NUMBER_OF_LENGTHS + 2,
	};

private:
	struct LengthType {
		SizeUnit Unit;
		short Size;
	};

public:
	ZLTextStyleEntry(unsigned char entryKind);
	//ZLTextStyleEntry(unsigned char entryKind, char *address);
	~ZLTextStyleEntry();

	unsigned char entryKind() const;

	bool isEmpty() const;
	bool isFeatureSupported(Feature featureId) const;

	//short length(Feature featureId, const Metrics &metrics) const;
	void setLength(Feature featureId, short length, SizeUnit unit);

	ZLTextAlignmentType alignmentType() const;
	void setAlignmentType(ZLTextAlignmentType alignmentType);

	ZLBoolean3 fontModifier(FontModifier modifier) const;
	void setFontModifier(FontModifier modifier, bool on);

	const std::string &fontFamily() const;
	void setFontFamily(const std::string &fontFamily);

private:
	const unsigned char myEntryKind;
	unsigned short myFeatureMask;

	LengthType myLengths[NUMBER_OF_LENGTHS];
	ZLTextAlignmentType myAlignmentType;
	unsigned char mySupportedFontModifier;
	unsigned char myFontModifier;
	std::string myFontFamily;

	friend class ZLTextModel;
};

inline ZLTextStyleEntry::ZLTextStyleEntry(unsigned char entryKind) : myEntryKind(entryKind), myFeatureMask(0), myAlignmentType(ALIGN_UNDEFINED), mySupportedFontModifier(0), myFontModifier(0) {}
inline ZLTextStyleEntry::~ZLTextStyleEntry() {}

inline unsigned char ZLTextStyleEntry::entryKind() const { return myEntryKind; }

inline ZLTextStyleEntry::Metrics::Metrics(int fontSize, int fontXHeight, int fullWidth, int fullHeight) : FontSize(fontSize), FontXHeight(fontXHeight), FullWidth(fullWidth), FullHeight(fullHeight) {}

inline bool ZLTextStyleEntry::isEmpty() const { return myFeatureMask == 0; }
inline bool ZLTextStyleEntry::isFeatureSupported(Feature featureId) const {
	return (myFeatureMask & (1 << featureId)) != 0;
}

inline void ZLTextStyleEntry::setLength(Feature featureId, short length, SizeUnit unit) {
	myFeatureMask |= 1 << featureId;
	myLengths[featureId].Size = length;
	myLengths[featureId].Unit = unit;
}

inline ZLTextAlignmentType ZLTextStyleEntry::alignmentType() const { return myAlignmentType; }
inline void ZLTextStyleEntry::setAlignmentType(ZLTextAlignmentType alignmentType) {
	myFeatureMask |= 1 << ALIGNMENT_TYPE;
	myAlignmentType = alignmentType;
}

inline ZLBoolean3 ZLTextStyleEntry::fontModifier(FontModifier modifier) const {
	if ((mySupportedFontModifier & modifier) == 0) {
		return B3_UNDEFINED;
	}
	return (myFontModifier & modifier) == 0 ? B3_FALSE : B3_TRUE;
}
inline void ZLTextStyleEntry::setFontModifier(FontModifier modifier, bool on) {
	myFeatureMask |= 1 << FONT_STYLE_MODIFIER;
	mySupportedFontModifier |= modifier;
	if (on) {
		myFontModifier |= modifier;
	} else {
		myFontModifier &= ~modifier;
	}
}

inline const std::string &ZLTextStyleEntry::fontFamily() const { return myFontFamily; }
inline void ZLTextStyleEntry::setFontFamily(const std::string &fontFamily) {
	myFeatureMask |= 1 << FONT_FAMILY;
	myFontFamily = fontFamily;
}

#endif /* __ZLTEXTSTYLEENTRY_H__ */
