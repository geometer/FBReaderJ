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

#include <ZLUnicodeUtil.h>

#include "DummyEncodingConverter.h"

class DummyEncodingConverter : public ZLEncodingConverter {

private:
	DummyEncodingConverter(const std::string &name);

public:
	~DummyEncodingConverter();
	std::string name() const;
	void convert(std::string &dst, const char *srcStart, const char *srcEnd);
	void reset();
	bool fillTable(int *map);

private:
	const std::string myName;

friend class DummyEncodingConverterProvider;
};

bool DummyEncodingConverterProvider::providesConverter(const std::string &encoding) {
	const std::string lowerCasedEncoding = ZLUnicodeUtil::toLower(encoding);
	return (lowerCasedEncoding == "utf-8") || (lowerCasedEncoding == "us-ascii");
}

shared_ptr<ZLEncodingConverter> DummyEncodingConverterProvider::createConverter(const std::string &name) {
	return new DummyEncodingConverter(name);
}

DummyEncodingConverter::DummyEncodingConverter(const std::string &name) : myName(name) {
}

DummyEncodingConverter::~DummyEncodingConverter() {
}

std::string DummyEncodingConverter::name() const {
	return myName;
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
