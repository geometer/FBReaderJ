/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

public final class TagTree extends LibraryTree {
	public final Tag Tag;

	TagTree(Tag tag) {
		Tag = tag;
	}

	TagTree(LibraryTree parent, Tag tag, int position) {
		super(parent, position);
		Tag = tag;
	}

	@Override
	public String getName() {
		return Tag != null
			? Tag.Name : Library.resource().getResource("booksWithNoTags").getValue();
	}

	@Override
	protected String getStringId() {
		return "@TagTree " + getName();
	}

	protected String getSortKey() {
		return (Tag != null) ? Tag.Name : null;
	}

	@Override
	public boolean containsBook(Book book) {
		if (book == null) {
			return false;
		}
		if (Tag == null) {
			return book.tags().isEmpty();
		}
		for (Tag t : book.tags()) {
			for (; t != null; t = t.Parent) {
				if (t == Tag) {
					return true;
				}
			}
		}
		return false;
	}
}
