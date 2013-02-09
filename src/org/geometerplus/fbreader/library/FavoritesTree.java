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
	FavoritesTree(RootTree root) {
		super(root, ROOT_FAVORITES);
	}

	@Override
	public Status getOpeningStatus() {
		if (!Collection.hasFavorites()) {
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
		for (Book book : Collection.favorites()) {
			new BookWithAuthorsTree(this, book);
		}
	}

	public boolean onBookEvent(BookEvent event, Book book) {
		if (event == BookEvent.Added && Collection.isFavorite(book)) {
			new BookWithAuthorsTree(this, book);
			return true;
		} if (event == BookEvent.Updated && !Collection.isFavorite(book)) {
			return removeBook(book);
		} else {
			return super.onBookEvent(event, book);
		}
	}
}
