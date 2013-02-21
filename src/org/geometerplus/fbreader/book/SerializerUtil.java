/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

public abstract class SerializerUtil {
	private SerializerUtil() {
	}

	private static final AbstractSerializer defaultSerializer = new XMLSerializer();

	public static String serialize(Book book) {
		return book != null ? defaultSerializer.serialize(book) : null;
	}

	public static Book deserializeBook(String xml) {
		return xml != null ? defaultSerializer.deserializeBook(xml) : null;
	}

	public static String serialize(Bookmark bookmark) {
		return bookmark != null ? defaultSerializer.serialize(bookmark) : null;
	}

	public static Bookmark deserializeBookmark(String xml) {
		return xml != null ? defaultSerializer.deserializeBookmark(xml) : null;
	}

	public static List<String> serializeBookList(List<Book> books) {
		final List<String> serialized = new ArrayList<String>(books.size());
		for (Book b : books) {
			serialized.add(defaultSerializer.serialize(b));
		}
		return serialized;
	}

	public static List<Book> deserializeBookList(List<String> xmlList) {
		final List<Book> books = new ArrayList<Book>(xmlList.size());
		for (String xml : xmlList) {
			final Book b = defaultSerializer.deserializeBook(xml);
			if (b != null) {
				books.add(b);
			}
		}
		return books;
	}

	public static List<String> serializeBookmarkList(List<Bookmark> bookmarks) {
		final List<String> serialized = new ArrayList<String>(bookmarks.size());
		for (Bookmark b : bookmarks) {
			serialized.add(defaultSerializer.serialize(b));
		}
		return serialized;
	}

	public static List<Bookmark> deserializeBookmarkList(List<String> xmlList) {
		final List<Bookmark> bookmarks = new ArrayList<Bookmark>(xmlList.size());
		for (String xml : xmlList) {
			final Bookmark b = defaultSerializer.deserializeBookmark(xml);
			if (b != null) {
				bookmarks.add(b);
			}
		}
		return bookmarks;
	}
}
