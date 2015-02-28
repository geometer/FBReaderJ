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

package org.geometerplus.android.fbreader.libraryService;

import org.geometerplus.fbreader.book.*;

abstract class Util {
	static String authorToString(Author author) {
		return author.DisplayName + "\000" + author.SortKey;
	}

	static Author stringToAuthor(String string) {
		if (string == null) {
			return Author.NULL;
		}

		final String[] split = string.split("\000");
		if (split.length != 2) {
			return Author.NULL;
		}

		return new Author(split[0], split[1]);
	}

	static String tagToString(Tag tag) {
		return tag.toString("\000");
	}

	static Tag stringToTag(String string) {
		if (string == null) {
			return Tag.NULL;
		}

		final String[] split = string.split("\000");
		if (split.length == 0) {
			return Tag.NULL;
		}

		return Tag.getTag(split);
	}

	static String formatDescriptorToString(IBookCollection.FormatDescriptor descriptor) {
		return descriptor.Id + "\000" + descriptor.Name + "\000" + (descriptor.IsActive ? 1 : 0);
	}

	static IBookCollection.FormatDescriptor stringToFormatDescriptor(String string) {
		if (string == null) {
			throw new IllegalArgumentException();
		}

		final String[] split = string.split("\000");
		if (split.length != 3) {
			throw new IllegalArgumentException();
		}

		final IBookCollection.FormatDescriptor descriptor = new IBookCollection.FormatDescriptor();
		descriptor.Id = split[0];
		descriptor.Name = split[1];
		descriptor.IsActive = "1".equals(split[2]);
		return descriptor;
	}
}
