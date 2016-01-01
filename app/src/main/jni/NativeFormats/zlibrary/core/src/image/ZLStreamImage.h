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

#ifndef __ZLSTREAMIMAGE_H__
#define __ZLSTREAMIMAGE_H__

#include <string>

#include <ZLImage.h>
#include <ZLInputStream.h>

class ZLStreamImage : public ZLSingleImage {

public:
	ZLStreamImage(const std::string &mimeType, const std::string &encoding, std::size_t offset, std::size_t size = 0);
	//const shared_ptr<std::string> stringData() const;

	const std::string &encoding() const;
	std::size_t offset() const;
	std::size_t size() const;

private:
	//virtual shared_ptr<ZLInputStream> inputStream() const = 0;

private:
	const std::string myEncoding;
	const std::size_t myOffset;
	mutable std::size_t mySize;
};

inline ZLStreamImage::ZLStreamImage(const std::string &mimeType, const std::string &encoding, std::size_t offset, std::size_t size) : ZLSingleImage(mimeType), myEncoding(encoding), myOffset(offset), mySize(size) {}
inline const std::string &ZLStreamImage::encoding() const { return myEncoding; }
inline std::size_t ZLStreamImage::offset() const { return myOffset; }
inline std::size_t ZLStreamImage::size() const { return mySize; }

#endif /* __ZLSTREAMIMAGE_H__ */
