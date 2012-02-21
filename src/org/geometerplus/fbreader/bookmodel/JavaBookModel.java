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

package org.geometerplus.fbreader.bookmodel;

import org.geometerplus.zlibrary.core.image.*;

import org.geometerplus.zlibrary.text.model.*;

import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.Paths;

public class JavaBookModel extends BookModel {
	public final ZLTextModel BookTextModel;

	JavaBookModel(Book book) {
		super(book);
		BookTextModel = new ZLTextWritablePlainModel(null, book.getLanguage(), 1024, 65536, Paths.cacheDirectory(), "cache", myImageMap);
	}

	@Override
	public ZLTextModel getTextModel() {
		return BookTextModel;
	}

	@Override
	public ZLTextModel getFootnoteModel(String id) {
		ZLTextModel model = myFootnotes.get(id);
		if (model == null) {
			model = new ZLTextWritablePlainModel(id, Book.getLanguage(), 8, 512, Paths.cacheDirectory(), "cache" + myFootnotes.size(), myImageMap);
			myFootnotes.put(id, model);
		}
		return model;
	}

	void addImage(String id, ZLImage image) {
		myImageMap.put(id, image);
	}
}
