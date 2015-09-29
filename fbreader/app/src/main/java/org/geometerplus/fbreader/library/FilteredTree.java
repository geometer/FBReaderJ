/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.util.List;

import org.geometerplus.zlibrary.core.util.MiscUtil;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.formats.PluginCollection;

abstract class FilteredTree extends LibraryTree {
	private final Filter myFilter;

	FilteredTree(IBookCollection collection, PluginCollection pluginCollection, Filter filter) {
		super(collection, pluginCollection);
		myFilter = filter;
	}

	FilteredTree(LibraryTree parent, Filter filter, int position) {
		super(parent, position);
		myFilter = filter;
	}

	@Override
	public String getSummary() {
		return MiscUtil.join(Collection.titles(new BookQuery(myFilter, 5)), ", ");
	}

	@Override
	public boolean containsBook(Book book) {
		return book != null && myFilter.matches(book);
	}

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	protected final void createBookSubtrees() {
		for (BookQuery query = new BookQuery(myFilter, 20); ; query = query.next()) {
			final List<Book> books = Collection.books(query);
			if (books.isEmpty()) {
				break;
			}
			for (Book b : books) {
				createSubtree(b);
			}
		}
	}

	@Override
	public void waitForOpening() {
		clear();
		createBookSubtrees();
	}

	@Override
	public boolean onBookEvent(BookEvent event, Book book) {
		switch (event) {
			case Added:
				return containsBook(book) && createSubtree(book);
			case Updated:
			{
				boolean changed = removeBook(book);
				changed |= containsBook(book) && createSubtree(book);
				return changed;
			}
			case Removed:
			default:
				return super.onBookEvent(event, book);
		}
	}

	protected abstract boolean createSubtree(Book book);
}
