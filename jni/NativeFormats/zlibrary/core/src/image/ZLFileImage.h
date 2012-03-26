/*
 * Copyright (C) 2004-2012 Geometer Plus <contact@geometerplus.com>
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

#ifndef __ZLFILEIMAGE_H__
#define __ZLFILEIMAGE_H__

#include <ZLFile.h>

#include "ZLStreamImage.h"

class ZLFileImage : public ZLStreamImage {

public:
	ZLFileImage(const ZLFile &file, const std::string &encoding, size_t offset, size_t size = 0);

	//Kind kind() const;
	const ZLFile &file() const;

protected:
	//shared_ptr<ZLInputStream> inputStream() const;

private:
	const ZLFile myFile;
};

inline ZLFileImage::ZLFileImage(const ZLFile &file, const std::string &encoding, size_t offset, size_t size) : ZLStreamImage(file.mimeType(), encoding, offset, size), myFile(file) {}
//inline ZLSingleImage::Kind ZLFileImage::kind() const { return FILE_IMAGE; }
inline const ZLFile &ZLFileImage::file() const { return myFile; }
//inline shared_ptr<ZLInputStream> ZLFileImage::inputStream() const { return myFile.inputStream(); }

#endif /* __ZLFILEIMAGE_H__ */
