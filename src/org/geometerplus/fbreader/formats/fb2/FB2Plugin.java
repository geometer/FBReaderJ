/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.fb2;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;

public class FB2Plugin extends FormatPlugin {
	@Override
	public boolean acceptsFile(ZLFile file) {
		return "fb2".equals(file.getExtension());
	}
	
	@Override
	public boolean readMetaInfo(Book book) {
		return new FB2MetaInfoReader(book).readMetaInfo();
	}
	
	@Override
	public boolean readModel(BookModel model) {
		return new FB2Reader(model).readBook();
	}

	@Override
	public ZLImage readCover(ZLFile file) {
		return new FB2CoverReader().readCover(file);
	}

	@Override
	public String readAnnotation(ZLFile file) {
		return new FB2AnnotationReader().readAnnotation(file);
	}
}
