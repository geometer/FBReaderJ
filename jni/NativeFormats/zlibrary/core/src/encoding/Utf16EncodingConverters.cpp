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

#include <ZLUnicodeUtil.h>

#include "Utf16EncodingConverters.h"

class Utf16EncodingConverter : public ZLEncodingConverter {

protected:
	Utf16EncodingConverter();

public:
	void reset();
	bool fillTable(int *map);
	void convert(std::string &dst, const char *srcStart, const char *srcEnd);

protected:
	virtual ZLUnicodeUtil::Ucs2Char ucs2Char(unsigned char c0, unsigned char c1) = 0;

private:
	bool myHasStoredChar;
	unsigned char myStoredChar;
};

class Utf16BEEncodingConverter : public Utf16EncodingConverter {

public:
	std::string name() const;

private:
	ZLUnicodeUtil::Ucs2Char ucs2Char(unsigned char c0, unsigned char c1);
};

class Utf16LEEncodingConverter : public Utf16EncodingConverter {

public:
	std::string name() const;

private:
	ZLUnicodeUtil::Ucs2Char ucs2Char(unsigned char c0, unsigned char c1);
};

bool Utf16EncodingConverterProvider::providesConverter(const std::string &encoding) {
	const std::string lowerCasedEncoding = ZLUnicodeUtil::toLower(encoding);
	return
		ZLEncodingConverter::UTF16 == lowerCasedEncoding ||
		ZLEncodingConverter::UTF16BE == lowerCasedEncoding;
}

shared_ptr<ZLEncodingConverter> Utf16EncodingConverterProvider::createConverter(const std::string &name) {
	if (ZLEncodingConverter::UTF16 == ZLUnicodeUtil::toLower(name)) {
		return new Utf16LEEncodingConverter();
	} else {
		return new Utf16BEEncodingConverter();
	}
}

Utf16EncodingConverter::Utf16EncodingConverter() : myHasStoredChar(false) {
}

void Utf16EncodingConverter::reset() {
	myHasStoredChar = false;
}

bool Utf16EncodingConverter::fillTable(int *map) {
	// is not possible
	return false;
}

void Utf16EncodingConverter::convert(std::string &dst, const char *srcStart, const char *srcEnd) {
	if (srcStart >= srcEnd) {
		return;
	}
	char buffer[3];
	if (myHasStoredChar) {
		dst.append(buffer, ZLUnicodeUtil::ucs2ToUtf8(buffer, ucs2Char(myStoredChar, *srcStart)));
		++srcStart;
		myHasStoredChar = false;
	}
	if ((srcEnd - srcStart) % 2 == 1) {
		--srcEnd;
		myStoredChar = (unsigned char)*srcEnd;
		myHasStoredChar = true;
	}
	for (; srcStart != srcEnd; srcStart += 2) {
		dst.append(buffer, ZLUnicodeUtil::ucs2ToUtf8(buffer, ucs2Char(*srcStart, *(srcStart + 1))));
	}
}

std::string Utf16LEEncodingConverter::name() const {
	return UTF16;
}

ZLUnicodeUtil::Ucs2Char Utf16LEEncodingConverter::ucs2Char(unsigned char c0, unsigned char c1) {
	return c0 + (((ZLUnicodeUtil::Ucs2Char)c1) << 8);
}

std::string Utf16BEEncodingConverter::name() const {
	return UTF16BE;
}

ZLUnicodeUtil::Ucs2Char Utf16BEEncodingConverter::ucs2Char(unsigned char c0, unsigned char c1) {
	return c1 + (((ZLUnicodeUtil::Ucs2Char)c0) << 8);
}
