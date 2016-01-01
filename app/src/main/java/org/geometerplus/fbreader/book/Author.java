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

public final class Author implements Comparable<Author> {
	public static final Author NULL = new Author("", "");

	public static Author create(String name, String sortKey) {
		if (name == null) {
			return null;
		}
		String strippedName = name.trim();
		if (strippedName.length() == 0) {
			return null;
		}

		String strippedKey = sortKey != null ? sortKey.trim() : "";
		if (strippedKey.length() == 0) {
			int index = strippedName.lastIndexOf(' ');
			if (index == -1) {
				strippedKey = strippedName;
			} else {
				strippedKey = strippedName.substring(index + 1);
				while ((index >= 0) && (strippedName.charAt(index) == ' ')) {
					--index;
				}
				strippedName = strippedName.substring(0, index + 1) + ' ' + strippedKey;
			}
		}

		return new Author(strippedName, strippedKey);
	}

	public final String DisplayName;
	public final String SortKey;

	public Author(String displayName, String sortKey) {
		DisplayName = displayName;
		SortKey = sortKey.toLowerCase();
	}

	public static int hashCode(Author author) {
		return author == null ? 0 : author.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Author)) {
			return false;
		}
		Author a = (Author)o;
		return SortKey.equals(a.SortKey) && DisplayName.equals(a.DisplayName);
	}

	@Override
	public int hashCode() {
		return SortKey.hashCode() + DisplayName.hashCode();
	}

	@Override
	public int compareTo(Author other) {
		final int byKeys = SortKey.compareTo(other.SortKey);
		return byKeys != 0 ? byKeys : DisplayName.compareTo(other.DisplayName);
	}

	@Override
	public String toString() {
		return DisplayName + " (" + SortKey + ")";
	}
}
