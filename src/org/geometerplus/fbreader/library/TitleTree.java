/*
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.library;

import org.geometerplus.fbreader.book.*;

public final class TitleTree extends LibraryTree {
	public final String Title;

	TitleTree(IBookCollection collection, String title) {
		super(collection);
		Title = title;
	}

	TitleTree(LibraryTree parent, String title, int position) {
		super(parent, position);
		Title = title;
	}

	@Override
	public String getName() {
		return Title;
	}

	@Override
	protected String getStringId() {
		return "@TitleTree " + getName();
	}

	@Override
	public boolean containsBook(Book book) {
		return Title.equals(TitleUtil.firstTitleLetter(book));
	}

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public void waitForOpening() {
		clear();

		for (Book b : Collection.booksForTitlePrefix(Title)) {
			createBookWithAuthorsSubTree(b);
		}
	}

	@Override
	public boolean onBookEvent(BookEvent event, Book book) {
		switch (event) {
			case Added:
				return
					Title.equals(TitleUtil.firstTitleLetter(book)) &&
					createBookWithAuthorsSubTree(book);
			case Removed:
				// TODO: implement
				return false;
			default:
			case Updated:
				// TODO: implement
				return false;
		}
	}
}
