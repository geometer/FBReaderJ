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

#include "PdbPlugin.h"
#include "../txt/TxtBookReader.h"
#include "../html/HtmlBookReader.h"
#include "HtmlMetainfoReader.h"
//#include "../util/TextFormatDetector.h"

#include "../../bookmodel/BookModel.h"
#include "../../library/Book.h"

bool SimplePdbPlugin::readMetainfo(Book &book) const {
	const ZLFile &file = book.file();
	shared_ptr<ZLInputStream> stream = createStream(file);
	detectEncodingAndLanguage(book, *stream);
	if (book.encoding().empty()) {
		return false;
	}
	int readType = HtmlMetainfoReader::NONE;
	if (book.title().empty()) {
		readType |= HtmlMetainfoReader::TITLE;
	}
	if (book.authors().empty()) {
		readType |= HtmlMetainfoReader::AUTHOR;
	}
	if (readType != HtmlMetainfoReader::NONE) {
	//if ((readType != HtmlMetainfoReader::NONE) && TextFormatDetector().isHtml(*stream)) {
		readType |= HtmlMetainfoReader::TAGS;
		HtmlMetainfoReader metainfoReader(book, (HtmlMetainfoReader::ReadType)readType);
		metainfoReader.readDocument(*stream);
	}

	return true;
}

/*
bool SimplePdbPlugin::readModel(BookModel &model) const {
	const Book &book = *model.book();
	const ZLFile &file = book.file();
	shared_ptr<ZLInputStream> stream = createStream(file);

	PlainTextFormat format(file);
	if (!format.initialized()) {
		PlainTextFormatDetector detector;
		detector.detect(*stream, format);
	}
	readDocumentInternal(file, model, format, book.encoding(), *stream);
	return true;
}
*/

void SimplePdbPlugin::readDocumentInternal(const ZLFile&, BookModel &model, const PlainTextFormat &format, const std::string &encoding, ZLInputStream &stream) const {
	//if (TextFormatDetector().isHtml(stream)) {
		HtmlBookReader("", model, format, encoding).readDocument(stream);
	//} else {
		//TxtBookReader(model, format, encoding).readDocument(stream);
	//}
}
