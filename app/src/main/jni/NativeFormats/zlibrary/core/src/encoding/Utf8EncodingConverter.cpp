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

#include <algorithm>

#include <ZLUnicodeUtil.h>

#include "Utf8EncodingConverter.h"

class Utf8EncodingConverter : public ZLEncodingConverter {

private:
	Utf8EncodingConverter();

public:
	~Utf8EncodingConverter();
	std::string name() const;
	void convert(std::string &dst, const char *srcStart, const char *srcEnd);
	void reset();
	bool fillTable(int *map);

private:
	std::string myBuffer;

friend class Utf8EncodingConverterProvider;
};

bool Utf8EncodingConverterProvider::providesConverter(const std::string &encoding) {
	return ZLUnicodeUtil::toLower(encoding) == ZLEncodingConverter::UTF8;
}

shared_ptr<ZLEncodingConverter> Utf8EncodingConverterProvider::createConverter(const std::string&) {
	return new Utf8EncodingConverter();
}

Utf8EncodingConverter::Utf8EncodingConverter() {
}

Utf8EncodingConverter::~Utf8EncodingConverter() {
}

std::string Utf8EncodingConverter::name() const {
	return ZLEncodingConverter::UTF8;
}

void Utf8EncodingConverter::convert(std::string &dst, const char *srcStart, const char *srcEnd) {
	if (myBuffer.size() > 0) {
		const std::size_t len = ZLUnicodeUtil::length(myBuffer, 1);
		if (len < myBuffer.size()) {
			return;
		}
		const std::size_t diff = std::min(len - myBuffer.size(), (std::size_t)(srcEnd - srcStart));
		myBuffer.append(srcStart, diff);
		srcStart += diff;
		if (myBuffer.size() == len) {
			dst += myBuffer;
			myBuffer.clear();
		}
	}
	for (const char *ptr = srcEnd - 1; ptr >= srcEnd - 6 && ptr >= srcStart; --ptr) {
		if ((*ptr & 0xC0) != 0x80) {
			if (ZLUnicodeUtil::length(ptr, 1) > srcEnd - ptr) {
				myBuffer.append(ptr, srcEnd - ptr);
				srcEnd = ptr;
			}
			break;
		}
	}
	dst.append(srcStart, srcEnd - srcStart);
}

void Utf8EncodingConverter::reset() {
	myBuffer.clear();
}

bool Utf8EncodingConverter::fillTable(int *map) {
	for (int i = 0; i < 255; ++i) {
		map[i] = i;
	}
	return true;
}
