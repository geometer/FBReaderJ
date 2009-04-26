/*
 * Copyright (C) 2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.collection;

public class BookTree extends CollectionTree {
	public final BookDescription Description;
	private final boolean myShowAuthors;

	BookTree(CollectionTree parent, BookDescription description, boolean showAuthors) {
		super(parent);
		Description = description;
		myShowAuthors = showAuthors;
	}

	public String getName() {
		return Description.getTitle();
	}

	private String myAuthorsString;
	public String getSecondString() {
		if (!myShowAuthors) {
			return super.getSecondString();
		}
		if (myAuthorsString == null) {
			StringBuilder builder = new StringBuilder();
			int count = 0;
			for (Author author : Description.authors()) {
				if (count++ > 0) {
					builder.append(",  ");
				}
				builder.append(author.DisplayName);
				if (count == 5) {
					break;
				}
			}
			myAuthorsString = builder.toString();
		}
		return myAuthorsString;
	}
}
