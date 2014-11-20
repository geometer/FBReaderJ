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

#include "EncodedTextReader.h"
#include <android/log.h>

EncodedTextReader::EncodedTextReader(const std::string &encoding) {
	ZLEncodingCollection &collection = ZLEncodingCollection::Instance();
	const shared_ptr<ZLEncodingConverter>& ptr = collection.converter(encoding);
	__android_log_print(ANDROID_LOG_DEBUG, "FBReader", "EncodedTextReader collection 0x%08x, converter 0x%08x", &collection, &ptr);
	myConverter = ptr;
	if (myConverter.isNull()) {
		myConverter = collection.defaultConverter();
	}
}

EncodedTextReader::~EncodedTextReader() {
}
