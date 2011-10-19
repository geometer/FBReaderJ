/*
 * Copyright (C) 2004-2011 Geometer Plus <contact@geometerplus.com>
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

#include <cctype>

#include <ZLStringUtil.h>
#include <ZLInputStream.h>
#include <ZLFile.h>

#include "RtfImage.h"

inline static char convertXDigit(char d) {
	if (isdigit(d)) {
		return d - '0';
	} else if (islower(d)) {
		return d - 'a' + 10;
	} else {
		return d - 'A' + 10;
	}
}

void RtfImage::read() const {
	shared_ptr<ZLInputStream> stream = ZLFile(myFileName).inputStream();
	if (!stream.isNull() && stream->open()) {
		myData = new std::string();
		myData->reserve(myLength / 2);
		stream->seek(myStartOffset, false);
		const size_t bufferSize = 1024;
		char *buffer = new char[bufferSize];
		for (unsigned int i = 0; i < myLength; i += bufferSize) {
			size_t toRead = std::min(bufferSize, myLength - i);
			if (stream->read(buffer, toRead) != toRead) {
				break;
			}
			for (size_t j = 0; j < toRead; j += 2) {
				*myData += (convertXDigit(buffer[j]) << 4) + convertXDigit(buffer[j + 1]);
			}
		}
		delete[] buffer;
		stream->close();
	}
}

const shared_ptr<std::string> RtfImage::stringData() const {
	if (myData.isNull()) {
		read();
	}
	return myData;
}
