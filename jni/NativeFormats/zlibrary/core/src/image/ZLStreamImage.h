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

#ifndef __ZLSTREAMIMAGE_H__
#define __ZLSTREAMIMAGE_H__

#include <string>

#include <ZLImage.h>
#include <ZLInputStream.h>

class ZLStreamImage : public ZLSingleImage {

public:
	ZLStreamImage(const std::string &mimeType, size_t offset, size_t size = 0);
	const shared_ptr<std::string> stringData() const;

private:
	virtual shared_ptr<ZLInputStream> inputStream() const = 0;

private:
	size_t myOffset;
	mutable size_t mySize;
};

inline ZLStreamImage::ZLStreamImage(const std::string &mimeType, size_t offset, size_t size) : ZLSingleImage(mimeType), myOffset(offset), mySize(size) {}

#endif /* __ZLSTREAMIMAGE_H__ */
