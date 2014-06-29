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

package org.geometerplus.fbreader.book;

import java.util.*;

import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.core.util.RationalNumber;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import org.geometerplus.fbreader.bookmodel.BookReadingException;

class BookMergeHelper {
	private final IBookCollection myCollection;

	BookMergeHelper(IBookCollection collection) {
		myCollection = collection;
	}

	boolean merge(Book base, Book duplicate) {
		boolean result = false;
		result |= mergeMetainfo(base, duplicate);
		result |= mergeBookmarks(base, duplicate, true);
		result |= mergeBookmarks(base, duplicate, false);
		result |= mergeLabels(base, duplicate);
		result |= mergePositions(base, duplicate);
		result |= mergeProgress(base, duplicate);
		if (result) {
			myCollection.saveBook(base);
		}
		myCollection.removeBook(duplicate, false);
		return result;
	}

	private boolean mergeMetainfo(Book base, Book duplicate) {
		if (base.hasSameMetainfoAs(duplicate)) {
			return false;
		}
		final Book vanilla;
		try {
			vanilla = new Book(base.File);
		} catch (BookReadingException e) {
			return false;
		}
		base.merge(duplicate, vanilla);
		return true;
	}

	private boolean mergeLabels(Book base, Book duplicate) {
		final List<String> labels = duplicate.labels();
		if (MiscUtil.listsEquals(labels, base.labels())) {
			return false;
		}
		for (String l : labels) {
			base.addLabel(l);
		}
		return true;
	}

	private boolean mergePositions(Book base, Book duplicate) {
		if (myCollection.getStoredPosition(base.getId()) != null) {
			return false;
		}
		final ZLTextPosition position = myCollection.getStoredPosition(duplicate.getId());
		if (position == null) {
			return false;
		}
		myCollection.storePosition(base.getId(), position);
		return true;
	}

	private boolean mergeProgress(Book base, Book duplicate) {
		if (base.getProgress() != null) {
			return false;
		}
		final RationalNumber progress = duplicate.getProgress();
		if (progress == null) {
			return false;
		}
		base.setProgress(progress);
		return true;
	}

	private List<Bookmark> allBookmarks(Book book, boolean visible) {
		List<Bookmark> result = null;
		for (BookmarkQuery query = new BookmarkQuery(book, visible, 20); ; query = query.next()) {
			final List<Bookmark> portion = myCollection.bookmarks(query);
			if (portion.isEmpty()) {
				break;
			}
			if (result == null) {
				result = new ArrayList<Bookmark>(portion);
			} else {
				result.addAll(portion);
			}
		}
		return result != null ? result : Collections.<Bookmark>emptyList();
	}

	private boolean hasSameBookmark(List<Bookmark> original, Bookmark bookmark) {
		for (Bookmark b : original) {
			if (b.sameAs(bookmark)) {
				return true;
			}
		}
		return false;
	}

	private boolean mergeBookmarks(Book base, Book duplicate, boolean visible) {
		final List<Bookmark> duplicateBookmarks = allBookmarks(duplicate, visible);
		if (duplicateBookmarks.isEmpty()) {
			return false;
		}
		final List<Bookmark> baseBookmarks = allBookmarks(base, visible);
		boolean result = false;
		for (Bookmark b : duplicateBookmarks) {
			if (!hasSameBookmark(baseBookmarks, b)) {
				final Bookmark clone = b.transferToBook(base);
				if (clone != null) {
					myCollection.saveBookmark(clone);
				}
				result = true;
			}
		}

		return result;
	}
}
