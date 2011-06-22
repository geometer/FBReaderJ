/*
 * Copyright (C) 2011 Geometer Plus <contact@geometerplus.com>
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

#ifndef __COVERSWRITER_H__
#define __COVERSWRITER_H__

#include <jni.h>

#include <string>
#include <map>

#include <shared_ptr.h>
#include <ZLImage.h>

class CoversWriter {

private:
	static shared_ptr<CoversWriter> ourInstance;

	CoversWriter();

public:
	static CoversWriter &Instance();

	void resetCounter();

	jobject writeCover(const std::string &bookPath, const ZLImage &image);

private:
	struct ImageData {
		std::string Path;
		size_t Offset;
		size_t Length;
		ImageData() : Offset(0), Length(0) {}
	};

	std::string makeFileName(size_t index) const;

	jobject writeSingleCover(const std::string &bookPath, const ZLSingleImage &image);
	bool fillSingleImageData(ImageData &imageData, const ZLSingleImage &image);

private:
	const std::string myFileExtension;
	size_t myCoversCounter;
	std::map<std::string, ImageData> myImageCache;

private:
	CoversWriter(const CoversWriter &);
	const CoversWriter &operator = (const CoversWriter &);
};

#endif /* __COVERSWRITER_H__ */
