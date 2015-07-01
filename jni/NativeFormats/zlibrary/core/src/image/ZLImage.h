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

#ifndef __ZLIMAGE_H__
#define __ZLIMAGE_H__

#include <string>

#include <shared_ptr.h>

class ZLImage {

protected:
	ZLImage();

public:
	virtual ~ZLImage();
	virtual bool isSingle() const = 0;
};

class ZLSingleImage : public ZLImage {

/*
public:
	enum Kind {
		REGULAR_IMAGE = 1,
		FILE_IMAGE = 2,
		BASE64_ENCODED_IMAGE = 3,
	};
*/

protected:
	ZLSingleImage(const std::string &mimeType);
	virtual ~ZLSingleImage();

public:
	bool isSingle() const { return true; }
	const std::string &mimeType() const;
	//virtual const shared_ptr<std::string> stringData() const = 0;

	//virtual Kind kind() const;

private:
	std::string myMimeType;
};

class ZLMultiImage : public ZLImage {

protected:
	ZLMultiImage();
	virtual ~ZLMultiImage();

public:
	bool isSingle() const { return false; }
	virtual unsigned int rows() const = 0;
	virtual unsigned int columns() const = 0;
	virtual shared_ptr<const ZLImage> subImage(unsigned int row, unsigned int column) const = 0;
};

inline ZLImage::ZLImage() {}
inline ZLImage::~ZLImage() {}

inline ZLSingleImage::ZLSingleImage(const std::string &mimeType) : myMimeType(mimeType) {}
inline ZLSingleImage::~ZLSingleImage() {}
inline const std::string &ZLSingleImage::mimeType() const { return myMimeType; }
//inline ZLSingleImage::Kind ZLSingleImage::kind() const { return REGULAR_IMAGE; }

inline ZLMultiImage::ZLMultiImage() : ZLImage() {}
inline ZLMultiImage::~ZLMultiImage() {}

#endif /* __ZLIMAGE_H__ */
