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

import java.util.List;

public abstract class Filter {
	public abstract boolean matches(Book book);

	public final static class Empty extends Filter {
		public boolean matches(Book book) {
			return true;
		}
	}

	public final static class ByAuthor extends Filter {
		public final Author Author;

		public ByAuthor(Author author) {
			Author = author;
		}

		public boolean matches(Book book) {
			final List<Author> bookAuthors = book.authors();
			return
				Author.NULL.equals(Author) ? bookAuthors.isEmpty() : bookAuthors.contains(Author);
		}
	}

	public final static class ByTag extends Filter {
		public final Tag Tag;

		public ByTag(Tag tag) {
			Tag = tag;
		}

		public boolean matches(Book book) {
			final List<Tag> bookTags = book.tags();
			return
				Tag.NULL.equals(Tag) ? bookTags.isEmpty() : bookTags.contains(Tag);
		}
	}

	public final static class ByLabel extends Filter {
		public final String Label;

		public ByLabel(String label) {
			Label = label;
		}

		public boolean matches(Book book) {
			return book.labels().contains(Label);
		}
	}

	public final static class ByPattern extends Filter {
		public final String Pattern;

		public ByPattern(String pattern) {
			Pattern = pattern != null ? pattern.toLowerCase() : "";
		}

		public boolean matches(Book book) {
			return book != null && !"".equals(Pattern) && book.matches(Pattern);
		}
	}

	public final static class ByTitlePrefix extends Filter {
		public final String Prefix;

		public ByTitlePrefix(String prefix) {
			Prefix = prefix != null ? prefix : "";
		}

		public boolean matches(Book book) {
			return book != null && Prefix.equals(book.firstTitleLetter());
		}
	}

	public final static class BySeries extends Filter {
		public final Series Series;

		public BySeries(Series series) {
			Series = series;
		}

		public boolean matches(Book book) {
			final SeriesInfo info = book.getSeriesInfo();
			return info != null && Series.equals(info.Series);
		}
	}

	public final static class HasBookmark extends Filter {
		public boolean matches(Book book) {
			return book != null && book.HasBookmark;
		}
	}

	public final static class HasPhysicalFile extends Filter {
		public boolean matches(Book book) {
			return book != null && book.File.getPhysicalFile() != null;
		}
	}

	public final static class And extends Filter {
		public final Filter First;
		public final Filter Second;

		public And(Filter first, Filter second) {
			First = first;
			Second = second;
		}

		public boolean matches(Book book) {
			return First.matches(book) && Second.matches(book);
		}
	}

	public final static class Or extends Filter {
		public final Filter First;
		public final Filter Second;

		public Or(Filter first, Filter second) {
			First = first;
			Second = second;
		}

		public boolean matches(Book book) {
			return First.matches(book) || Second.matches(book);
		}
	}

	public final static class Not extends Filter {
		public final Filter Base;

		public Not(Filter base) {
			Base = base;
		}

		public boolean matches(Book book) {
			return !Base.matches(book);
		}
	}
}
