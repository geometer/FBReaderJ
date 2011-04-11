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

#ifndef __ZLIMAGEMANAGER_H__
#define __ZLIMAGEMANAGER_H__

#include <string>

#include <shared_ptr.h>

class ZLImage;
class ZLMultiImage;

class ZLImageData {

protected:
	ZLImageData() {}
	
public:
	virtual ~ZLImageData() {}

	virtual unsigned int width() const = 0;
	virtual unsigned int height() const = 0;

	virtual void init(unsigned int width, unsigned int height) = 0;
	virtual void setPosition(unsigned int x, unsigned int y) = 0;
	virtual void moveX(int delta) = 0;
	virtual void moveY(int delta) = 0;
	virtual void setPixel(unsigned char r, unsigned char g, unsigned char b) = 0;
	void setGrayPixel(unsigned char c);

	virtual void copyFrom(const ZLImageData &source, unsigned int targetX, unsigned int targetY) = 0;
};

class ZLImageManager {

public:
	static const ZLImageManager &Instance();
	static void deleteInstance();

protected:
	static ZLImageManager *ourInstance;
	
public:
	shared_ptr<ZLImageData> imageData(const ZLImage &image) const;

protected:
	ZLImageManager() {}
	virtual ~ZLImageManager() {}

	virtual shared_ptr<ZLImageData> createData() const = 0;
	virtual bool convertImageDirect(const std::string &stringData, ZLImageData &imageData) const = 0;

private:
	bool convertMultiImage(const ZLMultiImage &multiImage, ZLImageData &imageData) const;
	bool convertFromPalmImageFormat(const std::string &imageString, ZLImageData &imageData) const;
};

inline void ZLImageData::setGrayPixel(unsigned char c) { setPixel(c, c, c); }

inline const ZLImageManager &ZLImageManager::Instance() { return *ourInstance; }

#endif /* __IMAGEMANAGER_H__ */
