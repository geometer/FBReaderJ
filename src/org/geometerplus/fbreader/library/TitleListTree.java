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

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;

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
		final List<String> titles = Collection.titles();
		if (titles.size() > 10) {
			for (String t : titles) {
				final String letter = TitleTree.firstTitleLetter(t);
				if (letter != null) {
					letterSet.add(letter);
				}
			}
			myDoGroupByFirstLetter = titles.size() > letterSet.size() * 5 / 4;
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
					final String letter = TitleTree.firstTitleLetter(book);
					return letter != null && createTitleSubTree(letter);
				} else {
					return createBookWithAuthorsSubTree(book);
				}
			case Removed:
				// TODO: implement
				return false;
			default:
			case Updated:
				// TODO: implement
				return false;
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
