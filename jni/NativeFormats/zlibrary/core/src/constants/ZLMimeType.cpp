/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
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

#include "ZLMimeType.h"

const std::string ZLMimeType::APPLICATION_XML = "application/xml";
const std::string ZLMimeType::APPLICATION_ZIP = "application/zip";
const std::string ZLMimeType::APPLICATION_EPUB_ZIP = "application/epub+zip";
const std::string ZLMimeType::APPLICATION_FB2_ZIP = "application/fb2+zip";
const std::string ZLMimeType::APPLICATION_MOBIPOCKET_EBOOK = "application/x-mobipocket-ebook";
const std::string ZLMimeType::APPLICATION_PDF = "application/pdf";
const std::string ZLMimeType::APPLICATION_CHM = "application/x-chm";
const std::string ZLMimeType::APPLICATION_PALM_DB = "application/x-palm-database";

const std::string ZLMimeType::APPLICATION_ATOM_XML = "application/atom+xml";
const std::string ZLMimeType::APPLICATION_ATOM_XML_ENTRY = "application/atom+xml;type=entry";
const std::string ZLMimeType::APPLICATION_LITRES_XML = "application/litres+xml";

const std::string ZLMimeType::APPLICATION_GZIP = "application/x-gzip";
const std::string ZLMimeType::APPLICATION_BZIP2 = "application/x-bzip";
const std::string ZLMimeType::APPLICATION_TAR = "application/x-tar";
const std::string ZLMimeType::APPLICATION_TAR_GZIP = "application/x-compressed-tar";
const std::string ZLMimeType::APPLICATION_TAR_BZIP2 = "application/x-bzip-compressed-tar";
const std::string ZLMimeType::APPLICATION_TAR_7Z = "application/x-7z-compressed";

const std::string ZLMimeType::IMAGE_PNG = "image/png";
const std::string ZLMimeType::IMAGE_JPEG = "image/jpeg";
const std::string ZLMimeType::IMAGE_SVG = "image/svg+xml";

const std::string ZLMimeType::TEXT_HTML = "text/html";
const std::string ZLMimeType::TEXT_PLAIN = "text/plain";

bool ZLMimeType::isImage(const std::string &mimeType) {
	return
		mimeType == IMAGE_PNG ||
		mimeType == IMAGE_JPEG ||
		mimeType == IMAGE_SVG;
}
