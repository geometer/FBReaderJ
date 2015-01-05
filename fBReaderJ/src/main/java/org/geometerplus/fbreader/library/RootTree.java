/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.fbreader.options.SyncOptions;

public class RootTree extends LibraryTree {
	public RootTree(IBookCollection collection) {
		super(collection);

		new FavoritesTree(this);
		new RecentBooksTree(this);
		new AuthorListTree(this);
		new TitleListTree(this);
		new SeriesListTree(this);
		new TagListTree(this);
		if (new SyncOptions().Enabled.getValue()) {
			new SyncTree(this);
		}
		new FileFirstLevelTree(this);
	}

	public LibraryTree getLibraryTree(LibraryTree.Key key) {
		if (key == null) {
			return null;
		}
		if (key.Parent == null) {
			return key.Id.equals(getUniqueKey().Id) ? this : null;
		}
		final LibraryTree parentTree = getLibraryTree(key.Parent);
		return parentTree != null ? (LibraryTree)parentTree.getSubtree(key.Id) : null;
	}

	public SearchResultsTree getSearchResultsTree() {
		return (SearchResultsTree)getSubtree(LibraryTree.ROOT_FOUND);
	}

	public SearchResultsTree createSearchResultsTree(String pattern) {
		return new SearchResultsTree(this, LibraryTree.ROOT_FOUND, pattern);
	}

	@Override
	public String getName() {
		return resource().getValue();
	}

	@Override
	public String getSummary() {
		return resource().getValue();
	}

	@Override
	protected String getStringId() {
		return "@FBReaderLibraryRoot";
	}
}
