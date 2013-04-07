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

public class Filter {
	public final class Empty extends Filter {
	}

	public final class ByBook extends Filter {
		public final Book Book;

		public ByBook(Book book) {
			Book = book;
		}
	}

	public final class ByAuthor extends Filter {
		public final Author Author;

		public ByAuthor(Author author) {
			Author = author;
		}
	}

	public final class ByPattern extends Filter {
		public final String Pattern;

		public ByPattern(String pattern) {
			Pattern = pattern;
		}
	}

	public final class ByTitlePrefix extends Filter {
		public final String Prefix;

		public ByTitlePrefix(String prefix) {
			Prefix = prefix;
		}
	}

	public final class BySeries extends Filter {
		public final Series Series;

		public BySeries(Series series) {
			Series = series;
		}
	}

	public final class HasBookmark extends Filter {
	}

	public final class And extends Filter {
		public final Filter First;
		public final Filter Second;

		public And(Filter first, Filter second) {
			First = first;
			Second = second;
		}
	}

	public final class Or extends Filter {
		public final Filter First;
		public final Filter Second;

		public Or(Filter first, Filter second) {
			First = first;
			Second = second;
		}
	}
}
