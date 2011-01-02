/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

public final class Author {
	public final String DisplayName;
	public final String SortKey;

	public Author(String displayName, String sortKey) {
		DisplayName = displayName;
		SortKey = sortKey;
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
}
