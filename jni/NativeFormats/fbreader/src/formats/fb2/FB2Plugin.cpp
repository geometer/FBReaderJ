/*
 * Copyright (C) 2004-2011 Geometer Plus <contact@geometerplus.com>
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
#include <ZLImage.h>

#include "FB2Plugin.h"
#include "FB2MetaInfoReader.h"
#include "FB2BookReader.h"
#include "FB2CoverReader.h"

bool FB2Plugin::acceptsFile(const ZLFile &file) const {
	return file.extension() == "fb2";
}

bool FB2Plugin::readMetaInfo(Book &book) const {
	return FB2MetaInfoReader(book).readMetaInfo();
}

bool FB2Plugin::readModel(BookModel &model) const {
	return FB2BookReader(model).readBook();
}

shared_ptr<ZLImage> FB2Plugin::coverImage(const ZLFile &file) const {
	return FB2CoverReader(file).readCover();
}
