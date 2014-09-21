/*
 * Copyright (C) 2004-2014 Geometer Plus <contact@geometerplus.com>
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

#include <ZLFile.h>
#include <ZLInputStream.h>
#include <ZLLogger.h>
#include <ZLImage.h>
#include <ZLEncodingConverter.h>

#include "DocPlugin.h"
#include "DocMetaInfoReader.h"
#include "DocBookReader.h"
#include "DocStreams.h"
#include "../../bookmodel/BookModel.h"
#include "../../library/Book.h"

DocPlugin::DocPlugin() {
}

DocPlugin::~DocPlugin() {
}

bool DocPlugin::providesMetainfo() const {
	return true;
}

const std::string DocPlugin::supportedFileType() const {
	return "MS Word document";
}

bool DocPlugin::acceptsFile(const ZLFile &file) const {
	return file.extension() == "doc";
}

bool DocPlugin::readMetainfo(Book &book) const {
	if (!DocMetaInfoReader(book).readMetainfo()) {
		return false;
	}

	shared_ptr<ZLInputStream> stream = new DocAnsiStream(book.file(), 50000);
	if (!detectEncodingAndLanguage(book, *stream)) {
		stream = new DocUcs2Stream(book.file(), 50000);
		detectLanguage(book, *stream, ZLEncodingConverter::UTF8, true);
	}

	return true;
}

bool DocPlugin::readUids(Book &/*book*/) const {
	return true;
}

bool DocPlugin::readLanguageAndEncoding(Book &/*book*/) const {
	return true;
}

bool DocPlugin::readModel(BookModel &model) const {
	return DocBookReader(model, model.book()->encoding()).readBook();
}
