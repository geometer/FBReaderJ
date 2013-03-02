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

import java.util.*;

import org.geometerplus.fbreader.book.*;

public class TitleListTree extends FirstLevelTree {
	private boolean myDoGroupByFirstLetter;

	TitleListTree(RootTree root) {
		super(root, ROOT_BY_TITLE);
	}

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public void waitForOpening() {
		clear();

		myDoGroupByFirstLetter = false;
		final TreeSet<String> letterSet = new TreeSet<String>();
		final List<Book> book = Collection.books();
		final List<String> keys = new ArrayList<String>();
		for (Book b : book) {
			keys.add(b.getInfo().getSortKey());
		}
		if (keys.size() > 9) {
			for (String t : keys) {
				final String letter = TitleUtil.firstLetter(t);
				if (letter != null) {
					letterSet.add(letter);
				}
			}
			myDoGroupByFirstLetter = keys.size() > letterSet.size() * 5 / 4;
		}

		if (myDoGroupByFirstLetter) {
			for (String letter : letterSet) {
				createTitleSubTree(letter);
			}
		} else {
			for (Book b : Collection.books()) {
				createBookWithAuthorsSubTree(b);
			}
		}
	}

	@Override
	public boolean onBookEvent(BookEvent event, Book book) {
		switch (event) {
			case Added:
				if (myDoGroupByFirstLetter) {
					final String letter = TitleUtil.firstTitleLetter(book);
					return letter != null && createTitleSubTree(letter);
				} else {
					return createBookWithAuthorsSubTree(book);
				}
			case Removed:
				if (myDoGroupByFirstLetter) {
					// TODO: remove old tree (?)
					return false;
				} else {
					return super.onBookEvent(event, book);
				}
			default:
			case Updated:
				if (myDoGroupByFirstLetter) {
					// TODO: remove old tree (?)
					final String letter = TitleUtil.firstTitleLetter(book);
					return letter != null && createTitleSubTree(letter);
				} else {
					boolean changed = removeBook(book);
					changed |= containsBook(book) && createBookWithAuthorsSubTree(book);
					return changed;
				}
		}
	}

	boolean createTitleSubTree(String title) {
		final TitleTree temp = new TitleTree(Collection, title);
		int position = Collections.binarySearch(subTrees(), temp);
		if (position >= 0) {
			return false;
		} else {
			new TitleTree(this, title, - position - 1);
			return true;
		}
	}
}
