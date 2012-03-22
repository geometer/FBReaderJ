/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.formats.*;
import java.util.*;

public class FB2ZipPlugin extends JavaFormatPlugin {

	public static ZLFile getTrueFile(ZLFile f) {
		final List<ZLFile> children = f.children();
		if (children.size() == 1) {
			final ZLFile child = children.get(0);
			if (child.getPath().endsWith(".fb2")) {
				return child;
			}
		}
		return null;
	}

	public FB2ZipPlugin() {
		super("fb2.zip");
	}

	@Override
	public void readMetaInfo(Book book) throws BookReadingException {
		if (getTrueFile(book.File) == null) {
			throw new BookReadingException("errorReadingFile", book.File);
		}
		new FB2MetaInfoReader(book, getTrueFile(book.File)).readMetaInfo();
	}

	@Override
	public void readModel(BookModel model) throws BookReadingException {
		if (getTrueFile(model.Book.File) == null) {
			throw new BookReadingException("errorReadingFile", model.Book.File);
		}
		new FB2Reader(model, getTrueFile(model.Book.File)).readBook();
	}

	@Override
	public ZLImage readCover(ZLFile file) {
		if (getTrueFile(file) == null) {
			return null;
		}
		return new FB2CoverReader().readCover(getTrueFile(file));
	}

	@Override
	public String readAnnotation(ZLFile file) {
		if (getTrueFile(file) == null) {
			return null;
		}
		return new FB2AnnotationReader().readAnnotation(getTrueFile(file));
	}

	@Override
	public EncodingCollection supportedEncodings() {
		return new AutoEncodingCollection();
	}

	@Override
	public void detectLanguageAndEncoding(Book book) {
		book.setEncoding("auto");
	}
}
