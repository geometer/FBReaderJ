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

#include <ZLFile.h>
#include <ZLInputStream.h>

#include "TxtPlugin.h"
#include "TxtBookReader.h"
#include "PlainTextFormat.h"

#include "../../bookmodel/BookModel.h"
#include "../../library/Book.h"

TxtPlugin::~TxtPlugin() {
}

bool TxtPlugin::providesMetainfo() const {
	return false;
}

const std::string TxtPlugin::supportedFileType() const {
	return "txt";
}

bool TxtPlugin::readMetainfo(Book &/*book*/) const {
	return true;
}

bool TxtPlugin::readUids(Book &/*book*/) const {
	return true;
}

bool TxtPlugin::readModel(BookModel &model) const {
	Book &book = *model.book();
	const ZLFile &file = book.file();
	shared_ptr<ZLInputStream> stream = file.inputStream();
	if (stream.isNull()) {
		return false;
	}

	PlainTextFormat format(file);
	if (!format.initialized()) {
		PlainTextFormatDetector detector;
		detector.detect(*stream, format);
	}

	readLanguageAndEncoding(book);
	TxtBookReader(model, format, book.encoding()).readDocument(*stream);
	return true;
}

//FormatInfoPage *TxtPlugin::createInfoPage(ZLOptionsDialog &dialog, const ZLFile &file) {
//	return new PlainTextInfoPage(dialog, file, ZLResourceKey("Text"), true);
//}

bool TxtPlugin::readLanguageAndEncoding(Book &book) const {
	shared_ptr<ZLInputStream> stream = book.file().inputStream();
	if (stream.isNull()) {
		return false;
	}
	detectEncodingAndLanguage(book, *stream);
	return !book.encoding().empty();
}
