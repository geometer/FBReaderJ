/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

#ifndef __ZLMIMETYPE_H__
#define __ZLMIMETYPE_H__

#include <string>

class ZLMimeType {

public:
	static const std::string APPLICATION_XML;
	static const std::string APPLICATION_ZIP;
	static const std::string APPLICATION_EPUB_ZIP;
	static const std::string APPLICATION_FB2_ZIP;
	static const std::string APPLICATION_MOBIPOCKET_EBOOK;
	static const std::string APPLICATION_PDF;
	static const std::string APPLICATION_CHM;
	static const std::string APPLICATION_PALM_DB;
	static const std::string APPLICATION_ATOM_XML;
	static const std::string APPLICATION_ATOM_XML_ENTRY;
	static const std::string APPLICATION_LITRES_XML;

	static const std::string APPLICATION_GZIP;
	static const std::string APPLICATION_BZIP2;
	static const std::string APPLICATION_TAR;
	static const std::string APPLICATION_TAR_GZIP;
	static const std::string APPLICATION_TAR_BZIP2;
	static const std::string APPLICATION_TAR_7Z;

	static const std::string IMAGE_PNG;
	static const std::string IMAGE_JPEG;
	static const std::string IMAGE_SVG;

	static const std::string TEXT_HTML;
	static const std::string TEXT_PLAIN;

public:
	static bool isImage(const std::string &mimeType);

private:
	ZLMimeType();
};

#endif /* __ZLMIMETYPE_H__ */
