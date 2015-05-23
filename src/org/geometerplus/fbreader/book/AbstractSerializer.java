/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

public abstract class AbstractSerializer {
	public interface BookCreator<B extends AbstractBook> {
		B createBook(long id, String url, String title, String encoding, String language);
	}

	public abstract String serialize(BookQuery query);
	public abstract BookQuery deserializeBookQuery(String data);

	public abstract String serialize(BookmarkQuery query);
	public abstract BookmarkQuery deserializeBookmarkQuery(String data, BookCreator<? extends AbstractBook> creator);

	public abstract String serialize(AbstractBook book);
	public abstract <B extends AbstractBook> B deserializeBook(String data, BookCreator<B> creator);

	public abstract String serialize(Bookmark bookmark);
	public abstract Bookmark deserializeBookmark(String data);

	public abstract String serialize(HighlightingStyle style);
	public abstract HighlightingStyle deserializeStyle(String data);
}
