/*
 * Copyright (C) 2004-2010 Geometer Plus <contact@geometerplus.com>
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

#include <ZLFile.h>
#include <ZLInputStream.h>

#include "ZLStreamImage.h"

const shared_ptr<std::string> ZLStreamImage::stringData() const {
	shared_ptr<ZLInputStream> stream = inputStream();
	if (stream.isNull() || !stream->open()) {
		return 0;
	}
	if (mySize == 0) {
		mySize = stream->sizeOfOpened();
		if (mySize == 0) {
			return 0;
		}
	}

	shared_ptr<std::string> imageData = new std::string();

	stream->seek(myOffset, false);
	char *buffer = new char[mySize];
	stream->read(buffer, mySize);
	imageData->append(buffer, mySize);
	delete[] buffer;

	return imageData;
}
