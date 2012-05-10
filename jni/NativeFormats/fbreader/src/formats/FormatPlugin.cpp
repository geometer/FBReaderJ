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

#include <jni.h>

#include <ZLInputStream.h>
#include <ZLLanguageDetector.h>
#include <ZLImage.h>

#include "FormatPlugin.h"

#include "../library/Book.h"

void FormatPlugin::detectEncodingAndLanguage(Book &book, ZLInputStream &stream) {
	std::string language = book.language();
	std::string encoding = book.encoding();

	if (!encoding.empty()) {
		return;
	}

	PluginCollection &collection = PluginCollection::Instance();
	if (encoding.empty()) {
		encoding = "utf-8";
	}
	if (collection.isLanguageAutoDetectEnabled() && stream.open()) {
		static const int BUFSIZE = 65536;
		char *buffer = new char[BUFSIZE];
		const size_t size = stream.read(buffer, BUFSIZE);
		stream.close();
		shared_ptr<ZLLanguageDetector::LanguageInfo> info = ZLLanguageDetector().findInfo(buffer, size);
		delete[] buffer;
		if (!info.isNull()) {
			if (!info->Language.empty()) {
				language = info->Language;
			}
			encoding = info->Encoding;
			if ((encoding == "us-ascii") || (encoding == "iso-8859-1")) {
				encoding = "windows-1252";
			}
		}
	}
	book.setEncoding(encoding);
	book.setLanguage(language);
}

void FormatPlugin::detectLanguage(Book &book, ZLInputStream &stream) {
	std::string language = book.language();
	if (!language.empty()) {
		return;
	}

	PluginCollection &collection = PluginCollection::Instance();
	if (collection.isLanguageAutoDetectEnabled() && stream.open()) {
		static const int BUFSIZE = 65536;
		char *buffer = new char[BUFSIZE];
		const size_t size = stream.read(buffer, BUFSIZE);
		stream.close();
		shared_ptr<ZLLanguageDetector::LanguageInfo> info =
			ZLLanguageDetector().findInfoForEncoding(book.encoding(), buffer, size, -20000);
		delete[] buffer;
		if (!info.isNull()) {
			if (!info->Language.empty()) {
				language = info->Language;
			}
		}
	}
	book.setLanguage(language);
}

const std::string &FormatPlugin::tryOpen(const ZLFile&) const {
	static const std::string EMPTY = "";
	return EMPTY;
}

shared_ptr<const ZLImage> FormatPlugin::coverImage(const ZLFile &file) const {
	return 0;
}
