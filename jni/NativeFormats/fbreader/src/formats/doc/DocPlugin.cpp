/*
 * Copyright (C) 2004-2010 Geometer Plus <contact@geometerplus.com>
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

#include <iostream>

#include <ZLFile.h>
#include <ZLInputStream.h>
#include <ZLLogger.h>
#include <ZLImage.h>

#include "DocPlugin.h"
#include "DocMetaInfoReader.h"
#include "DocBookReader.h"
#include "../../bookmodel/BookModel.h"
#include "../../library/Book.h"

DocPlugin::DocPlugin() {
}

DocPlugin::~DocPlugin() {
}

bool DocPlugin::providesMetaInfo() const {
	return true;
}

const std::string DocPlugin::supportedFileType() const {
	return "doc";
}

bool DocPlugin::acceptsFile(const ZLFile &file) const {
	return file.extension() == "doc";
}

bool DocPlugin::readMetaInfo(Book &book) const {
	return DocMetaInfoReader(book).readMetaInfo();
}

bool DocPlugin::readLanguageAndEncoding(Book &/*book*/) const {
	return true;
}

bool DocPlugin::readModel(BookModel &model) const {
	return DocBookReader(model, model.book()->encoding()).readBook();
}
