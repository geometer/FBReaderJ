/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.libraryService;

import java.util.*;

import android.os.RemoteException;

import org.geometerplus.fbreader.library.*;

public class LibraryShadow extends AbstractLibrary {
	private final LibraryInterface myInterface;

	public LibraryShadow(LibraryInterface iface) {
		myInterface = iface;
	}

	@Override
	public boolean isUpToDate() {
		try {
			return myInterface.isUpToDate();
		} catch (RemoteException e) {
			return false;
		}
	}

	@Override
	public boolean canRemoveBookFile(Book book) {
		// TODO: implement
		return false;
	}

	@Override
	public void removeBook(Book book, int removeMode) {
		// TODO: implement
	}

	public Book getRecentBook() {
		// TODO: implement
		return null;
	}

	public Book getPreviousBook() {
		// TODO: implement
		return null;
	}

	public void addBookToRecentList(Book book) {
		// TODO: implement
	}

	@Override
	public boolean isBookInFavorites(Book book) {
		// TODO: implement
		return false;
	}

	@Override
	public void addBookToFavorites(Book book) {
		// TODO: implement
	}

	@Override
	public void removeBookFromFavorites(Book book) {
		// TODO: implement
	}

	@Override
	public void startBookSearch(final String pattern) {
		// TODO: implement
	}

	@Override
	public List<Bookmark> allBookmarks() {
		// TODO: implement
		return Collections.emptyList();
	}

	@Override
	public List<Bookmark> invisibleBookmarks(Book book) {
		// TODO: implement
		return Collections.emptyList();
	}
}
