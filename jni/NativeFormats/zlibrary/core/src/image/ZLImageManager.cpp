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

#include <algorithm>
#include <vector>

#include "ZLImage.h"
#include "ZLImageManager.h"

ZLImageManager *ZLImageManager::ourInstance = 0;

void ZLImageManager::deleteInstance() {
	if (ourInstance != 0) {
		delete ourInstance;
		ourInstance = 0;
	}
}

struct Color {
	unsigned char Red;
	unsigned char Green;
	unsigned char Blue;
};

struct PalmImageHeader {
	PalmImageHeader(const std::string &str);
	
	unsigned short Width;
	unsigned short Height;
	unsigned short BytesPerRow;
	unsigned short Flags;
	unsigned char BitsPerPixel;
	unsigned char CompressionType;
};

static Color PalmImage8bitColormap[256] = {
	{ 255, 255, 255 }, { 255, 204, 255 }, { 255, 153, 255 }, { 255, 102, 255 }, 
	{ 255,  51, 255 }, { 255,   0, 255 }, { 255, 255, 204 }, { 255, 204, 204 }, 
	{ 255, 153, 204 }, { 255, 102, 204 }, { 255,  51, 204 }, { 255,   0, 204 }, 
	{ 255, 255, 153 }, { 255, 204, 153 }, { 255, 153, 153 }, { 255, 102, 153 }, 
	{ 255,  51, 153 }, { 255,   0, 153 }, { 204, 255, 255 }, { 204, 204, 255 },
	{ 204, 153, 255 }, { 204, 102, 255 }, { 204,  51, 255 }, { 204,   0, 255 },
	{ 204, 255, 204 }, { 204, 204, 204 }, { 204, 153, 204 }, { 204, 102, 204 },
	{ 204,  51, 204 }, { 204,   0, 204 }, { 204, 255, 153 }, { 204, 204, 153 },
	{ 204, 153, 153 }, { 204, 102, 153 }, { 204,  51, 153 }, { 204,   0, 153 },
	{ 153, 255, 255 }, { 153, 204, 255 }, { 153, 153, 255 }, { 153, 102, 255 },
	{ 153,  51, 255 }, { 153,   0, 255 }, { 153, 255, 204 }, { 153, 204, 204 },
	{ 153, 153, 204 }, { 153, 102, 204 }, { 153,  51, 204 }, { 153,   0, 204 },
	{ 153, 255, 153 }, { 153, 204, 153 }, { 153, 153, 153 }, { 153, 102, 153 },
	{ 153,  51, 153 }, { 153,   0, 153 }, { 102, 255, 255 }, { 102, 204, 255 },
	{ 102, 153, 255 }, { 102, 102, 255 }, { 102,  51, 255 }, { 102,   0, 255 },
	{ 102, 255, 204 }, { 102, 204, 204 }, { 102, 153, 204 }, { 102, 102, 204 },
	{ 102,  51, 204 }, { 102,   0, 204 }, { 102, 255, 153 }, { 102, 204, 153 },
	{ 102, 153, 153 }, { 102, 102, 153 }, { 102,  51, 153 }, { 102,   0, 153 },
	{  51, 255, 255 }, {  51, 204, 255 }, {  51, 153, 255 }, {  51, 102, 255 },
	{  51,  51, 255 }, {  51,   0, 255 }, {  51, 255, 204 }, {  51, 204, 204 },
	{  51, 153, 204 }, {  51, 102, 204 }, {  51,  51, 204 }, {  51,   0, 204 },
	{  51, 255, 153 }, {  51, 204, 153 }, {  51, 153, 153 }, {  51, 102, 153 },
	{  51,  51, 153 }, {  51,   0, 153 }, {   0, 255, 255 }, {   0, 204, 255 },
	{   0, 153, 255 }, {   0, 102, 255 }, {   0,  51, 255 }, {   0,   0, 255 },
	{   0, 255, 204 }, {   0, 204, 204 }, {   0, 153, 204 }, {   0, 102, 204 },
	{   0,  51, 204 }, {   0,   0, 204 }, {   0, 255, 153 }, {   0, 204, 153 },
	{   0, 153, 153 }, {   0, 102, 153 }, {   0,  51, 153 }, {   0,   0, 153 },
	{ 255, 255, 102 }, { 255, 204, 102 }, { 255, 153, 102 }, { 255, 102, 102 },
	{ 255,  51, 102 }, { 255,   0, 102 }, { 255, 255,  51 }, { 255, 204,  51 },
	{ 255, 153,  51 }, { 255, 102,  51 }, { 255,  51,  51 }, { 255,   0,  51 },
	{ 255, 255,   0 }, { 255, 204,   0 }, { 255, 153,   0 }, { 255, 102,   0 },
	{ 255,  51,   0 }, { 255,   0,   0 }, { 204, 255, 102 }, { 204, 204, 102 },
	{ 204, 153, 102 }, { 204, 102, 102 }, { 204,  51, 102 }, { 204,   0, 102 },
	{ 204, 255,  51 }, { 204, 204,  51 }, { 204, 153,  51 }, { 204, 102,  51 },
	{ 204,  51,  51 }, { 204,   0,  51 }, { 204, 255,   0 }, { 204, 204,   0 },
	{ 204, 153,   0 }, { 204, 102,   0 }, { 204,  51,   0 }, { 204,   0,   0 },
	{ 153, 255, 102 }, { 153, 204, 102 }, { 153, 153, 102 }, { 153, 102, 102 },
	{ 153,  51, 102 }, { 153,   0, 102 }, { 153, 255,  51 }, { 153, 204,  51 },
	{ 153, 153,  51 }, { 153, 102,  51 }, { 153,  51,  51 }, { 153,   0,  51 },
	{ 153, 255,   0 }, { 153, 204,   0 }, { 153, 153,   0 }, { 153, 102,   0 },
	{ 153,  51,   0 }, { 153,   0,   0 }, { 102, 255, 102 }, { 102, 204, 102 },
	{ 102, 153, 102 }, { 102, 102, 102 }, { 102,  51, 102 }, { 102,   0, 102 },
	{ 102, 255,  51 }, { 102, 204,  51 }, { 102, 153,  51 }, { 102, 102,  51 },
	{ 102,  51,  51 }, { 102,   0,  51 }, { 102, 255,   0 }, { 102, 204,   0 },
	{ 102, 153,   0 }, { 102, 102,   0 }, { 102,  51,   0 }, { 102,   0,   0 },
	{  51, 255, 102 }, {  51, 204, 102 }, {  51, 153, 102 }, {  51, 102, 102 },
	{  51,  51, 102 }, {  51,   0, 102 }, {  51, 255,  51 }, {  51, 204,  51 },
	{  51, 153,  51 }, {  51, 102,  51 }, {  51,  51,  51 }, {  51,   0,  51 },
	{  51, 255,   0 }, {  51, 204,   0 }, {  51, 153,   0 }, {  51, 102,   0 },
	{  51,  51,   0 }, {  51,   0,   0 }, {   0, 255, 102 }, {   0, 204, 102 },
	{   0, 153, 102 }, {   0, 102, 102 }, {   0,  51, 102 }, {   0,   0, 102 },
	{   0, 255,  51 }, {   0, 204,  51 }, {   0, 153,  51 }, {   0, 102,  51 },
	{   0,  51,  51 }, {   0,   0,  51 }, {   0, 255,   0 }, {   0, 204,   0 },
	{   0, 153,   0 }, {   0, 102,   0 }, {   0,  51,   0 }, {  17,  17,  17 },
	{  34,  34,  34 }, {  68,  68,  68 }, {  85,  85,  85 }, { 119, 119, 119 },
	{ 136, 136, 136 }, { 170, 170, 170 }, { 187, 187, 187 }, { 221, 221, 221 },
	{ 238, 238, 238 }, { 192, 192, 192 }, { 128,   0,   0 }, { 128,   0, 128 },
	{   0, 128,   0 }, {   0, 128, 128 }, {   0,   0,   0 }, {   0,   0,   0 },
	{   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 },
	{   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 },
	{   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 },
	{   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 },
	{   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 },
	{   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }, {   0,   0,   0 }
};

inline static unsigned short uShort(const std::string &imageString, size_t offset) {
	return 256 * (unsigned char)imageString[offset] + (unsigned char)imageString[offset + 1];
}

PalmImageHeader::PalmImageHeader(const std::string &str) {
	Width = ::uShort(str, 0);
	Height = ::uShort(str, 2);
	BytesPerRow = ::uShort(str, 4);
	Flags = ::uShort(str, 6);
	BitsPerPixel = str[8];
	CompressionType = (Flags & 0x8000) ? str[13] : 0xFF;
}

bool ZLImageManager::convertFromPalmImageFormat(const std::string &imageString, ZLImageData &imageData) const {
	if (imageString.length() >= 16) {
		PalmImageHeader header(imageString);
		switch (header.CompressionType) {
			case 0x00: // scanline
				//std::cerr << "scanline encoded images are not supported yet\n";
				break;
			case 0x01: // rle
				//std::cerr << "rle encoded images are not supported yet\n";
				break;
			case 0x02: //packbits
				//std::cerr << "packbits encoded images are not supported yet\n";
				break;
			case 0xFF: // none
				if (imageString.length() >= (size_t)header.BytesPerRow * header.Height + 16) {
					if ((header.BitsPerPixel != 1) &&
							(header.BitsPerPixel != 2) &&
							(header.BitsPerPixel != 4) &&
							(header.BitsPerPixel != 8) &&
							(header.BitsPerPixel != 16)) {
						//std::cerr << "images with bpp = " << (int)header.BitsPerPixel << " are not supported\n";
						break;
					}

					imageData.init(header.Width, header.Height);

					if (header.BitsPerPixel == 16) {
						const unsigned char redBits = imageString[16];
						const unsigned char greenBits = imageString[17];
						const unsigned char blueBits = imageString[18];
						const unsigned short redMask = (1 << redBits) - 1;
						const unsigned short greenMask = (1 << greenBits) - 1;
						const unsigned short blueMask = (1 << blueBits) - 1;

						const unsigned char *from_ptr = (const unsigned char*)imageString.data() + 24;
						for (unsigned short i = 0; i < header.Height; ++i) {
							const unsigned char *to_ptr = from_ptr + header.BytesPerRow;
							imageData.setPosition(0, i);
							for (; from_ptr < to_ptr; from_ptr += 2) {
								unsigned short color = 256 * *from_ptr + *(from_ptr + 1);
								imageData.setPixel(
									(color >> (16 - redBits)) * 255 / redMask,
									((color >> blueBits) & greenMask) * 255 / greenMask,
									(color & blueMask) * 255 / blueMask
								);
								imageData.moveX(1);
							}
						}
					} else {
						const unsigned char *from = (const unsigned char*)imageString.data() + 16;
						for (unsigned short i = 0; i < header.Height; ++i) {
							const unsigned char *from_ptr = from + header.BytesPerRow * i;
							imageData.setPosition(0, i);
							for (int j = 0; j < (int)header.Width; j += 8 / header.BitsPerPixel, ++from_ptr) {
								switch (header.BitsPerPixel) {
									case 1:
										{
											unsigned char len = std::min(8, (int)header.Width - j);
											for (unsigned char k = 0; k < len; ++k) {
												imageData.setGrayPixel(((*from_ptr) & (128 >> k)) ? 0 : 255);
												imageData.moveX(1);
											}
										}
										break;
									case 2:
										{
											unsigned char len = std::min(4, (int)header.Width - j);
											for (unsigned char k = 0; k < len; ++k) {
												imageData.setGrayPixel(85 * (3 - ((*from_ptr >> (6 - 2 * k)) & 0x3)));
												imageData.moveX(1);
											}
										}
										break;
									case 4:
										{
											imageData.setGrayPixel(17 * (15 - (*from_ptr >> 4)));
											imageData.moveX(1);
											if (j != (int)header.Width - 1) {
												imageData.setGrayPixel(17 * (15 - (*from_ptr & 0xF)));
												imageData.moveX(1);
											}
										}
										break;
									case 8:
										{
											const Color &col = PalmImage8bitColormap[*from_ptr];
											imageData.setPixel(col.Red, col.Green, col.Blue);
											imageData.moveX(1);
										}
										break;
								}
							}
						}
					}
					return true;
				}
				break;
			default: // unknown
				//std::cerr << "unknown image encoding: " << (int)header.CompressionType << "\n";
				break;
		}
	}
	return false;
}

bool ZLImageManager::convertMultiImage(const ZLMultiImage &multiImage, ZLImageData &data) const {
	unsigned int rows = multiImage.rows();
	unsigned int columns = multiImage.columns();
	if (rows == 0 || columns == 0) {
		return false;
	}

	std::vector<shared_ptr<ZLImageData> > parts;
	parts.reserve(rows * columns);
	std::vector<int> widths;
	widths.reserve(columns);
	std::vector<int> heights;
	heights.reserve(rows);
	int fullWidth = 0;
	int fullHeight = 0;

	for (unsigned int i = 0; i < rows; ++i) {
		for (unsigned int j = 0; j < columns; ++j) {
			shared_ptr<const ZLImage> subImage = multiImage.subImage(i, j);
			if (subImage.isNull()) {
				return false;
			}
			shared_ptr<ZLImageData> data = imageData(*subImage);
			if (data.isNull()) {
				return false;
			}
			int w = data->width();
			if (i == 0) {
				widths.push_back(w);
				fullWidth += w;
			} else if (w != widths[j]) {
				return false;
			}
			int h = data->height();
			if (j == 0) {
				heights.push_back(h);
				fullHeight += h;
			} else if (h != heights[i]) {
				return false;
			}
			parts.push_back(data);
		}
	}

	data.init(fullWidth, fullHeight);
	int vOffset = 0;
	for (unsigned int i = 0; i < rows; ++i) {
		int hOffset = 0;
		for (unsigned int j = 0; j < columns; ++j) {
			data.copyFrom(*parts[j * rows + i], hOffset, vOffset);
			hOffset += widths[j];
		}
		vOffset += heights[i];
	}
	return true;
}

shared_ptr<ZLImageData> ZLImageManager::imageData(const ZLImage &image) const {
	shared_ptr<ZLImageData> data;

	if (image.isSingle()) {
		const ZLSingleImage &singleImage = (const ZLSingleImage&)image;
		shared_ptr<std::string> stringData = singleImage.stringData();
		if (stringData.isNull() || stringData->empty()) {
			return 0;
		}
		data = createData();
		if (singleImage.mimeType() == "image/palm") {
			if (!convertFromPalmImageFormat(*stringData, *data)) {
				return 0;
			}
		} else {
			if (!convertImageDirect(*stringData, *data)) {
				return 0;
			}
		}
	} else {
		data = createData();
		if (!convertMultiImage((const ZLMultiImage&)image, *data)) {
			return 0;
		}
	}

	return data;
}
