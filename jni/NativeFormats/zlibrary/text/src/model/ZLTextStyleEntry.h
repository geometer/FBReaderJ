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

#ifndef __ZLTEXTSTYLEENTRY_H__
#define __ZLTEXTSTYLEENTRY_H__

#include <string>

#include <ZLTextParagraph.h>
#include <ZLTextAlignmentType.h>

class ZLTextStyleEntry : public ZLTextParagraphEntry {

public:
	enum SizeUnit {
		SIZE_UNIT_PIXEL,
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
		FONT_MODIFIER_DEFAULT =             0,
		FONT_MODIFIER_BOLD =           1 << 0,
		FONT_MODIFIER_ITALIC =         1 << 1,
		FONT_MODIFIER_UNDERLINED =     1 << 2,
		FONT_MODIFIER_STRIKEDTHROUGH = 1 << 3,
		FONT_MODIFIER_SMALLCAPS =      1 << 4,
	};

	enum Length {
		LENGTH_LEFT_INDENT =                0,
		LENGTH_RIGHT_INDENT =               1,
		LENGTH_FIRST_LINE_INDENT_DELTA =    2,
		LENGTH_SPACE_BEFORE =               3,
		LENGTH_SPACE_AFTER =                4,
		NUMBER_OF_LENGTHS =                 5,
	};

	static const unsigned int SUPPORTS_ALIGNMENT_TYPE =  1U <<  NUMBER_OF_LENGTHS;
	static const unsigned int SUPPORTS_FONT_SIZE_MAG =   1U << (NUMBER_OF_LENGTHS + 1);
	static const unsigned int SUPPORTS_FONT_FAMILY =     1U << (NUMBER_OF_LENGTHS + 2);

private:
	struct LengthType {
		SizeUnit Unit;
		short Size;
	};

public:
	ZLTextStyleEntry();
	ZLTextStyleEntry(char *address);
	~ZLTextStyleEntry();

	bool isEmpty() const;

	bool isLengthSupported(Length name) const;
	short length(Length name, const Metrics &metrics) const;
	void setLength(Length name, short length, SizeUnit unit);

	bool isAlignmentTypeSupported() const;
	ZLTextAlignmentType alignmentType() const;
	void setAlignmentType(ZLTextAlignmentType alignmentType);

	unsigned char supportedFontModifier() const;
	unsigned char fontModifier() const;
	void setFontModifier(FontModifier style, bool set);

	bool isFontSizeMagSupported() const;
	signed char fontSizeMag() const;
	void setFontSizeMag(signed char fontSizeMag);

	bool isFontFamilySupported() const;
	const std::string &fontFamily() const;
	void setFontFamily(const std::string &fontFamily);

private:
	unsigned int myMask;

	LengthType myLengths[NUMBER_OF_LENGTHS];

	ZLTextAlignmentType myAlignmentType;
	unsigned char mySupportedFontModifier;
	unsigned char myFontModifier;
	signed char myFontSizeMag;
	std::string myFontFamily;

friend class ZLTextModel;
};

inline ZLTextStyleEntry::ZLTextStyleEntry() : myMask(0), myAlignmentType(ALIGN_UNDEFINED), mySupportedFontModifier(0), myFontModifier(0), myFontSizeMag(0) {}
inline ZLTextStyleEntry::~ZLTextStyleEntry() {}

inline ZLTextStyleEntry::Metrics::Metrics(int fontSize, int fontXHeight, int fullWidth, int fullHeight) : FontSize(fontSize), FontXHeight(fontXHeight), FullWidth(fullWidth), FullHeight(fullHeight) {}

inline bool ZLTextStyleEntry::isEmpty() const { return myMask == 0; }

inline bool ZLTextStyleEntry::isLengthSupported(Length name) const { return (myMask & (1U << name)) != 0; }
inline void ZLTextStyleEntry::setLength(Length name, short length, SizeUnit unit) {
	myLengths[name].Size = length;
	myLengths[name].Unit = unit;
	myMask |= 1U << name;
}

inline bool ZLTextStyleEntry::isAlignmentTypeSupported() const { return (myMask & SUPPORTS_ALIGNMENT_TYPE) == SUPPORTS_ALIGNMENT_TYPE; }
inline ZLTextAlignmentType ZLTextStyleEntry::alignmentType() const { return myAlignmentType; }
inline void ZLTextStyleEntry::setAlignmentType(ZLTextAlignmentType alignmentType) { myAlignmentType = alignmentType; myMask |= SUPPORTS_ALIGNMENT_TYPE; }

inline unsigned char ZLTextStyleEntry::supportedFontModifier() const { return mySupportedFontModifier; }
inline unsigned char ZLTextStyleEntry::fontModifier() const { return myFontModifier; }
inline void ZLTextStyleEntry::setFontModifier(FontModifier style, bool set) {
	if (set) {
		myFontModifier |= style;
	} else {
		myFontModifier &= ~style;
	}
	mySupportedFontModifier |= style;
}

inline bool ZLTextStyleEntry::isFontSizeMagSupported() const { return (myMask & SUPPORTS_FONT_SIZE_MAG) == SUPPORTS_FONT_SIZE_MAG; }
inline signed char ZLTextStyleEntry::fontSizeMag() const { return myFontSizeMag; }
inline void ZLTextStyleEntry::setFontSizeMag(signed char fontSizeMag) { myFontSizeMag = fontSizeMag; myMask |= SUPPORTS_FONT_SIZE_MAG; }

inline bool ZLTextStyleEntry::isFontFamilySupported() const { return (myMask & SUPPORTS_FONT_FAMILY) == SUPPORTS_FONT_FAMILY; }
inline const std::string &ZLTextStyleEntry::fontFamily() const { return myFontFamily; }
inline void ZLTextStyleEntry::setFontFamily(const std::string &fontFamily) { myFontFamily = fontFamily; myMask |= SUPPORTS_FONT_FAMILY; }

#endif /* __ZLTEXTSTYLEENTRY_H__ */
