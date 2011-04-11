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

#ifndef __ZLBASE64ENCODEDIMAGE_H__
#define __ZLBASE64ENCODEDIMAGE_H__

#include <vector>

#include <ZLImage.h>

class ZLBase64EncodedImage : public ZLSingleImage {

public:
	ZLBase64EncodedImage(const std::string &mimeType);
	~ZLBase64EncodedImage();
	void addData(const std::string &text, size_t offset, size_t len);
	void addData(const std::vector<std::string> &text);
	const shared_ptr<std::string> stringData() const;

private:
	void decode() const;

private:
	mutable std::string myEncodedData;
	mutable shared_ptr<std::string> myData;
};

inline ZLBase64EncodedImage::ZLBase64EncodedImage(const std::string &mimeType) : ZLSingleImage(mimeType) {}
inline ZLBase64EncodedImage::~ZLBase64EncodedImage() {}

#endif /* __ZLBASE64ENCODEDIMAGE_H__ */
