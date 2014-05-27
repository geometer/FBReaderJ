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

package org.geometerplus.android.fbreader.libraryService;

import org.geometerplus.fbreader.book.Author;
import org.geometerplus.fbreader.book.Tag;

abstract class Util {
	static String authorToString(Author author) {
		return new StringBuilder(author.DisplayName).append('\000').append(author.SortKey).toString();
	}

	static Author stringToAuthor(String string) {
		final String[] split = string.split("\000");
		if (split.length == 2) {
			return new Author(split[0], split[1]);
		} else {
			return Author.NULL;
		}
	}

	static String tagToString(Tag tag) {
		return tag.toString("\000");
	}

	static Tag stringToTag(String string) {
		final String[] split = string.split("\000");
		if (split.length > 0) {
			return Tag.getTag(split);
		} else {
			return Tag.NULL;
		}
	}
}
