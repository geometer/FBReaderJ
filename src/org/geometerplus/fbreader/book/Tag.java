/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.util.HashMap;

public final class Tag {
	public static final Tag NULL = new Tag(null, "");

	private static final HashMap<Tag,Tag> ourTagSet = new HashMap<Tag,Tag>();

	public static Tag getTag(Tag parent, String name) {
		if (name == null) {
			return parent;
		}
		name = name.trim();
		if (name.length() == 0) {
			return parent == null ? Tag.NULL : parent;
		}
		Tag tag = new Tag(parent, name);
		Tag stored = ourTagSet.get(tag);
		if (stored != null) {
			return stored;
		}
		ourTagSet.put(tag, tag);
		return tag;
	}

	public static Tag getTag(String[] names) {
		return getTag(names, names.length);
	}

	private static Tag getTag(String[] names, int count) {
		return count == 0 ? null : getTag(getTag(names, count - 1), names[count - 1]);
	}

	public final Tag Parent;
	public final String Name;

	private Tag(Tag parent, String name) {
		Parent = parent;
		Name = name;
	}

	public String toString(String delimiter) {
		return toStringBuilder(delimiter).toString();
	}

	protected StringBuilder toStringBuilder(String delimiter) {
		return Parent == null
			? new StringBuilder(Name)
			: Parent.toStringBuilder(delimiter).append(delimiter).append(Name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Tag)) {
			return false;
		}
		Tag t = (Tag)o;
		return (Parent == t.Parent) && Name.equals(t.Name);
	}

	@Override
	public int hashCode() {
		return (Parent == null) ? Name.hashCode() : Parent.hashCode() + Name.hashCode();
	}
}
