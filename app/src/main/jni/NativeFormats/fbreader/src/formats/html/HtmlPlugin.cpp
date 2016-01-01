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

#include <ZLStringUtil.h>
#include <ZLFile.h>
#include <ZLInputStream.h>

#include "HtmlPlugin.h"
#include "HtmlDescriptionReader.h"
#include "HtmlBookReader.h"
#include "HtmlReaderStream.h"
#include "../txt/PlainTextFormat.h"
#include "../util/MiscUtil.h"
#include "../../library/Book.h"
#include "../../bookmodel/BookModel.h"

const std::string HtmlPlugin::supportedFileType() const {
	return "HTML";
}

bool HtmlPlugin::readMetainfo(Book &book) const {
	shared_ptr<ZLInputStream> stream = book.file().inputStream();
	if (stream.isNull()) {
		return false;
	}

	shared_ptr<ZLInputStream> htmlStream = new HtmlReaderStream(stream, 50000);
	detectEncodingAndLanguage(book, *htmlStream);
	if (book.encoding().empty()) {
		return false;
	}
	HtmlDescriptionReader(book).readDocument(*stream);

	return true;
}

bool HtmlPlugin::readUids(Book &/*book*/) const {
	return true;
}

bool HtmlPlugin::readModel(BookModel &model) const {
	const Book& book = *model.book();
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

	std::string directoryPrefix = MiscUtil::htmlDirectoryPrefix(file.path());
	HtmlBookReader reader(directoryPrefix, model, format, book.encoding());
	reader.setFileName(MiscUtil::htmlFileName(file.path()));
	reader.readDocument(*stream);

	return true;
}

//FormatInfoPage *HtmlPlugin::createInfoPage(ZLOptionsDialog &dialog, const ZLFile &file) {
//	return new PlainTextInfoPage(dialog, file, ZLResourceKey("<PRE>"), false);
//}

bool HtmlPlugin::readLanguageAndEncoding(Book &book) const {
	return true;
}
