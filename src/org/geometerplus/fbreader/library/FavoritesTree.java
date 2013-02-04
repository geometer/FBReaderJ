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

public class FavoritesTree extends FirstLevelTree {
	private final IBookCollection myCollection;

	FavoritesTree(IBookCollection collection, RootTree root) {
		super(root, Library.ROOT_FAVORITES);
		myCollection = collection;
	}

	@Override
	public Status getOpeningStatus() {
		if (!myCollection.hasFavorites()) {
			return Status.CANNOT_OPEN;
		}
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public String getOpeningStatusMessage() {
		return getOpeningStatus() == Status.CANNOT_OPEN
			? "noFavorites" : super.getOpeningStatusMessage();
	}

	@Override
	public void waitForOpening() {
		clear();
		for (Book book : myCollection.favorites()) {
			new BookTree(this, book, true);
		}
	}

	public boolean onBookEvent(BookEvent event, Book book) {
		if (event == BookEvent.Added && myCollection.isFavorite(book)) {
			new BookTree(this, book, true);
			return true;
		} if (event == BookEvent.Updated && !myCollection.isFavorite(book)) {
			return removeBook(book, false);
		} else {
			return super.onBookEvent(event, book);
		}
	}
}
