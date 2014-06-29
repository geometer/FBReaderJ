/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.text.model.*;
import org.geometerplus.fbreader.book.Book;

public class PluginBookModel extends BookModelImpl {
	PluginBookModel(Book book) {
		super(book);
	}

	@Override
	public ZLTextModel getTextModel() {
		return null;
	}

	@Override
	public ZLTextModel getFootnoteModel(String id) {
		return null;
	}

	@Override
	public boolean isValid() {
		return false;
	}
}
