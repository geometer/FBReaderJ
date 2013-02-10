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

package org.geometerplus.fbreader.library;

import java.util.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.tree.FBTree;

public final class Library {
	public static ZLResource resource() {
		return ZLResource.resource("library");
	}

	public final IBookCollection Collection;
	public final RootTree RootTree;

	public Library(IBookCollection collection) {
		Collection = collection;

		RootTree = new RootTree(collection);

		new FavoritesTree(RootTree);
		new RecentBooksTree(RootTree);
		new AuthorListTree(RootTree);
		new TitleListTree(RootTree);
		new SeriesListTree(RootTree);
		new TagListTree(RootTree);
		new FileFirstLevelTree(RootTree);
	}

	public LibraryTree getLibraryTree(LibraryTree.Key key) {
		if (key == null) {
			return null;
		}
		if (key.Parent == null) {
			return key.Id.equals(RootTree.getUniqueKey().Id) ? RootTree : null;
		}
		final LibraryTree parentTree = getLibraryTree(key.Parent);
		return parentTree != null ? (LibraryTree)parentTree.getSubTree(key.Id) : null;
	}

	public SearchResultsTree getSearchResultsTree() {
		return (SearchResultsTree)RootTree.getSubTree(LibraryTree.ROOT_FOUND);
	}

	public SearchResultsTree createSearchResultsTree(String pattern) {
		return new SearchResultsTree(RootTree, LibraryTree.ROOT_FOUND, pattern);
	}
}
