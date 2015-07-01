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

#include "ZLEncodingConverter.h"
#include "ZLEncodingConverterProvider.h"

const std::string ZLEncodingConverter::ASCII = "us-ascii";
const std::string ZLEncodingConverter::UTF8 = "utf-8";
const std::string ZLEncodingConverter::UTF16 = "utf-16";
const std::string ZLEncodingConverter::UTF16BE = "utf-16be";

ZLEncodingConverterProvider::ZLEncodingConverterProvider() {
}

ZLEncodingConverterProvider::~ZLEncodingConverterProvider() {
}

ZLEncodingConverter::ZLEncodingConverter() {
}

ZLEncodingConverter::~ZLEncodingConverter() {
}

void ZLEncodingConverter::convert(std::string &dst, const std::string &src) {
	convert(dst, src.data(), src.data() + src.length());
}
