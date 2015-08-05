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

import java.util.List;

import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.fbreader.options.SyncOptions;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.tree.FBTree;

public class RootTree extends LibraryTree {
	public RootTree(IBookCollection collection, PluginCollection pluginCollection) {
		super(collection, pluginCollection);

		//new ExternalViewTree(this);
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
		final int position;
		final List<FBTree> children = subtrees();
		if (children.isEmpty()) {
			position = 0;
		} else {
			position = children.get(0) instanceof ExternalViewTree ? 1 : 0;
		}
		return new SearchResultsTree(this, LibraryTree.ROOT_FOUND, pattern, position);
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
