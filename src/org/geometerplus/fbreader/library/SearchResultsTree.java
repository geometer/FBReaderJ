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

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;

public class SearchResultsTree extends FirstLevelTree {
	public final String Pattern;

	SearchResultsTree(RootTree root, String id, String pattern) {
		super(root, 0, id);
		Pattern = pattern != null ? pattern : "";
	}

	@Override
	public String getSummary() {
		return super.getSummary().replace("%s", Pattern);
	}

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public void waitForOpening() {
		clear();
		for (Book book : Collection.booksForPattern(Pattern)) {
			createBookWithAuthorsSubTree(book);
		}
	}

	@Override
	public boolean onBookEvent(BookEvent event, Book book) {
		switch (event) {
			case Added:
				return book.matches(Pattern) && createBookWithAuthorsSubTree(book);
			case Updated:
			{
				boolean changed = removeBook(book);
				changed |= book.matches(Pattern) && createBookWithAuthorsSubTree(book);
				return changed;
			}
			default:
				return super.onBookEvent(event, book);
		}
	}
}
