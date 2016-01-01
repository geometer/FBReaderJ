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

package org.geometerplus.fbreader.library;

import java.util.Collections;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.formats.PluginCollection;

public final class SeriesTree extends FilteredTree {
	public final Series Series;

	private static Filter filter(Series series, Author author) {
		final Filter f = new Filter.BySeries(series);
		return author != null ? new Filter.And(f, new Filter.ByAuthor(author)) : f;
	}

	SeriesTree(IBookCollection collection, PluginCollection pluginCollection, Series series, Author author) {
		super(collection, pluginCollection, filter(series, author));
		Series = series;
	}

	SeriesTree(LibraryTree parent, Series series, Author author, int position) {
		super(parent, filter(series, author), position);
		Series = series;
	}

	@Override
	public String getName() {
		return Series.getTitle();
	}

	@Override
	protected String getStringId() {
		return "@SeriesTree " + getName();
	}

	@Override
	protected String getSortKey() {
		return Series.getSortKey();
	}

	@Override
	protected boolean createSubtree(Book book) {
		final BookInSeriesTree temp = new BookInSeriesTree(Collection, PluginCollection, book);
		int position = Collections.binarySearch(subtrees(), temp);
		if (position >= 0) {
			return false;
		} else {
			new BookInSeriesTree(this, book, - position - 1);
			return true;
		}
	}
}
