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

#include <jni.h>

#include <ZLInputStream.h>
#include <ZLLanguageDetector.h>
#include <ZLImage.h>
#include <ZLEncodingConverter.h>

#include "FormatPlugin.h"

#include "../library/Book.h"

bool FormatPlugin::detectEncodingAndLanguage(Book &book, ZLInputStream &stream, bool force) {
	std::string language = book.language();
	std::string encoding = book.encoding();

	if (!force && !encoding.empty()) {
		return true;
	}

	bool detected = false;
	PluginCollection &collection = PluginCollection::Instance();
	if (encoding.empty()) {
		encoding = ZLEncodingConverter::UTF8;
	}
	if (collection.isLanguageAutoDetectEnabled() && stream.open()) {
		static const int BUFSIZE = 65536;
		char *buffer = new char[BUFSIZE];
		const std::size_t size = stream.read(buffer, BUFSIZE);
		stream.close();
		shared_ptr<ZLLanguageDetector::LanguageInfo> info = ZLLanguageDetector().findInfo(buffer, size);
		delete[] buffer;
		if (!info.isNull()) {
			detected = true;
			if (!info->Language.empty()) {
				language = info->Language;
			}
			encoding = info->Encoding;
			if (encoding == ZLEncodingConverter::ASCII || encoding == "iso-8859-1") {
				encoding = "windows-1252";
			}
		}
	}
	book.setEncoding(encoding);
	book.setLanguage(language);

	return detected;
}

bool FormatPlugin::detectLanguage(Book &book, ZLInputStream &stream, const std::string &encoding, bool force) {
	std::string language = book.language();
	if (!force && !language.empty()) {
		return true;
	}

	bool detected = false;

	PluginCollection &collection = PluginCollection::Instance();
	if (collection.isLanguageAutoDetectEnabled() && stream.open()) {
		static const int BUFSIZE = 65536;
		char *buffer = new char[BUFSIZE];
		const std::size_t size = stream.read(buffer, BUFSIZE);
		stream.close();
		shared_ptr<ZLLanguageDetector::LanguageInfo> info =
			ZLLanguageDetector().findInfoForEncoding(encoding, buffer, size, -20000);
		delete[] buffer;
		if (!info.isNull()) {
			detected = true;
			if (!info->Language.empty()) {
				language = info->Language;
			}
		}
	}
	book.setLanguage(language);

	return detected;
}

/*
const std::string &FormatPlugin::tryOpen(const ZLFile&) const {
	static const std::string EMPTY = "";
	return EMPTY;
}
*/

std::vector<shared_ptr<FileEncryptionInfo> > FormatPlugin::readEncryptionInfos(Book &book) const {
	return std::vector<shared_ptr<FileEncryptionInfo> >();
}

shared_ptr<const ZLImage> FormatPlugin::coverImage(const ZLFile &file) const {
	return 0;
}

std::string FormatPlugin::readAnnotation(const ZLFile &file) const {
	return "";
}
