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

#include "DummyEncodingConverter.h"

class DummyEncodingConverter : public ZLEncodingConverter {

private:
	DummyEncodingConverter();

public:
	~DummyEncodingConverter();
	std::string name() const;
	void convert(std::string &dst, const char *srcStart, const char *srcEnd);
	void reset();
	bool fillTable(int *map);

friend class DummyEncodingConverterProvider;
};

bool DummyEncodingConverterProvider::providesConverter(const std::string &encoding) {
	return ZLUnicodeUtil::toLower(encoding) == ZLEncodingConverter::ASCII;
}

shared_ptr<ZLEncodingConverter> DummyEncodingConverterProvider::createConverter(const std::string&) {
	return new DummyEncodingConverter();
}

DummyEncodingConverter::DummyEncodingConverter() {
}

DummyEncodingConverter::~DummyEncodingConverter() {
}

std::string DummyEncodingConverter::name() const {
	return ZLEncodingConverter::ASCII;
}

void DummyEncodingConverter::convert(std::string &dst, const char *srcStart, const char *srcEnd) {
	dst.append(srcStart, srcEnd - srcStart);
}

void DummyEncodingConverter::reset() {
}

bool DummyEncodingConverter::fillTable(int *map) {
	for (int i = 0; i < 255; ++i) {
		map[i] = i;
	}
	return true;
}
