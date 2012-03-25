/*
 * Copyright (C) 2011-2012 Geometer Plus <contact@geometerplus.com>
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

#include <ZLFileImage.h>

#include <ZLUnicodeUtil.h>

#include "ZLImageMapWriter.h"

ZLImageMapWriter::ZLImageMapWriter(const size_t rowSize,
		const std::string &directoryName, const std::string &fileExtension) :
	myAllocator(rowSize, directoryName, fileExtension) {
}


void ZLImageMapWriter::addImage(const std::string &id, const ZLImage &image) {
	const size_t dataSize = myAllocator.blocksNumber();
	const size_t bytesOffset = myAllocator.currentBytesOffset();

	myIds.push_back(id);
	myIndices.push_back((dataSize == 0) ? 0 : (dataSize - 1));
	myOffsets.push_back(bytesOffset / 2); // offset in words for future use in Java

	if (image.isSingle()) {
		addSingleImageEntry((const ZLFileImage&)image);
	} else {
		addMultiImageEntry((const ZLMultiImage&)image);
	}
}

void ZLImageMapWriter::addSingleImageEntry(const ZLFileImage &image) {
	ZLUnicodeUtil::Ucs2String mime;
	ZLUnicodeUtil::utf8ToUcs2(mime, image.mimeType());
	ZLUnicodeUtil::Ucs2String path;
	ZLUnicodeUtil::utf8ToUcs2(path, image.file().path());
	ZLUnicodeUtil::Ucs2String encoding;
	ZLUnicodeUtil::utf8ToUcs2(encoding, image.encoding());

	const size_t len = 16 + mime.size() * 2 + path.size() * 2 + encoding.size() * 2;
	char *ptr = myAllocator.allocate(len);

	*ptr++ = 1;//image.kind();
	*ptr++ = 0; // multi ? 1 : 0

	ptr = ZLCachedMemoryAllocator::writeString(ptr, mime);
	ptr = ZLCachedMemoryAllocator::writeString(ptr, path);
	ptr = ZLCachedMemoryAllocator::writeString(ptr, encoding);

	ptr = ZLCachedMemoryAllocator::writeUInt32(ptr, image.offset());
	ptr = ZLCachedMemoryAllocator::writeUInt32(ptr, image.size());
}

void ZLImageMapWriter::addMultiImageEntry(const ZLMultiImage &image) {
	const size_t len = 2;
	char *address = myAllocator.allocate(len);

	char *ptr = address;
	*ptr++ = 0; // kind -- N/A for multi images
	*ptr++ = 1; // multi ? 1 : 0

	// TODO: implement
}

void ZLImageMapWriter::flush() {
	myAllocator.flush();
}
