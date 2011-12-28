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
		addSingleImageEntry((const ZLSingleImage&)image);
	} else {
		addMultiImageEntry((const ZLMultiImage&)image);
	}
}

void ZLImageMapWriter::addSingleImageEntry(const ZLSingleImage &image) {
	ZLUnicodeUtil::Ucs2String ucs2mime;
	ZLUnicodeUtil::utf8ToUcs2(ucs2mime, image.mimeType());
	const size_t mimeSize = ucs2mime.size() * 2;

	const size_t len = 4 + mimeSize;
	char *address = myAllocator.allocate(len);

	char *ptr = address;
	*ptr++ = image.kind();
	*ptr++ = 0; // multi ? 1 : 0
	ZLCachedMemoryAllocator::writeUInt16(ptr, ucs2mime.size());
	memcpy(ptr + 2, &ucs2mime.front(), mimeSize);

	switch (image.kind()) {
		case ZLSingleImage::BASE64_ENCODED_IMAGE:
		case ZLSingleImage::REGULAR_IMAGE:
		{
			const shared_ptr<std::string> data = image.stringData();
			size_t length = data.isNull() ? 0 : data->length();
			size_t dataSize = (length + 1) / 2;

			const size_t newlen = len + 4 + dataSize * 2;
			address = myAllocator.reallocateLast(address, newlen);
			ptr = address + len;

			ZLCachedMemoryAllocator::writeUInt32(ptr, dataSize);
			ptr += 4;
			if (length > 0) {
				memcpy(ptr, data->data(), length);
				ptr += length;
				if (length % 2) {
					*ptr++ = 0;
				}
			}
			break;
		}
		case ZLSingleImage::FILE_IMAGE:
		{
			const ZLFileImage &fileImage = (const ZLFileImage&)image;

			ZLUnicodeUtil::Ucs2String ucs2path;
			ZLUnicodeUtil::utf8ToUcs2(ucs2path, fileImage.file().path());
			const size_t pathSize = ucs2path.size() * 2;

			const size_t newlen = len + 10 + pathSize;
			address = myAllocator.reallocateLast(address, newlen);
			ptr = address + len;

			ZLCachedMemoryAllocator::writeUInt32(ptr, fileImage.offset());
			ptr += 4;
			ZLCachedMemoryAllocator::writeUInt32(ptr, fileImage.size());
			ptr += 4;
			ZLCachedMemoryAllocator::writeUInt16(ptr, ucs2path.size());
			ptr += 2;
			memcpy(ptr, &ucs2path.front(), pathSize);
			ptr += pathSize;
			break;
		}
	}
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
