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

import org.geometerplus.zlibrary.core.util.MiscUtil;

import org.geometerplus.fbreader.book.*;

abstract class FilteredTree extends LibraryTree {
	final Filter myFilter;

	FilteredTree(IBookCollection collection, Filter filter) {
		super(collection);
		myFilter = filter;
	}

	FilteredTree(LibraryTree parent, Filter filter, int position) {
		super(parent, position);
		myFilter = filter;
	}

	@Override
	public final String getSummary() {
		return MiscUtil.join(Collection.titles(new Query(myFilter, 5)), ", ");
	}

	@Override
	public boolean containsBook(Book book) {
		return book != null && myFilter.matches(book);
	}

	@Override
	public final Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public void waitForOpening() {
		clear();
		for (Book book : Collection.books(new Query(myFilter, 1000))) {
			createSubTree(book);
		}
	}

	@Override
	public boolean onBookEvent(BookEvent event, Book book) {
		switch (event) {
			case Added:
				return containsBook(book) && createSubTree(book);
			case Updated:
			{
				boolean changed = removeBook(book);
				changed |= containsBook(book) && createSubTree(book);
				return changed;
			}
			case Removed:
			default:
				return super.onBookEvent(event, book);
		}
	}

	protected abstract boolean createSubTree(Book book);
}
