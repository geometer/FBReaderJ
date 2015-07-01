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
#include <ZLEncodingConverter.h>

#include "RtfPlugin.h"
#include "RtfDescriptionReader.h"
#include "RtfBookReader.h"
#include "RtfReaderStream.h"

#include "../../bookmodel/BookModel.h"
#include "../../library/Book.h"

bool RtfPlugin::providesMetainfo() const {
	return false;
}

const std::string RtfPlugin::supportedFileType() const {
	return "RTF";
}

bool RtfPlugin::readMetainfo(Book &book) const {
	readLanguageAndEncoding(book);

	if (!RtfDescriptionReader(book).readDocument(book.file())) {
		return false;
	}

	return true;
}

bool RtfPlugin::readUids(Book &/*book*/) const {
	return true;
}

bool RtfPlugin::readModel(BookModel &model) const {
	const Book &book = *model.book();
	return RtfBookReader(model, book.encoding()).readDocument(book.file());
}

bool RtfPlugin::readLanguageAndEncoding(Book &book) const {
	if (book.encoding().empty()) {
		shared_ptr<ZLInputStream> stream = new RtfReaderStream(book.file(), 50000);
		if (!stream.isNull()) {
			detectEncodingAndLanguage(book, *stream);
		}
		if (book.encoding().empty()) {
			book.setEncoding(ZLEncodingConverter::UTF8);
		}
	} else if (book.language().empty()) {
		shared_ptr<ZLInputStream> stream = new RtfReaderStream(book.file(), 50000);
		if (!stream.isNull()) {
			detectLanguage(book, *stream, book.encoding());
		}
	}
	return true;
}
