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

import org.fbreader.util.Pair;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.book.*;

public class SearchResultsTree extends FilteredTree {
	public final String Pattern;
	private final String myId;
	private final ZLResource myResource;

	SearchResultsTree(RootTree root, String id, String pattern, int position) {
		super(root, new Filter.ByPattern(pattern), position);
		myId = id;
		myResource = resource().getResource(myId);
		Pattern = pattern != null ? pattern : "";
	}

	@Override
	public String getName() {
		return myResource.getValue();
	}

	@Override
	public Pair<String,String> getTreeTitle() {
		return new Pair(getSummary(), null);
	}

	@Override
	protected String getStringId() {
		return myId;
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	@Override
	public String getSummary() {
		return myResource.getResource("summary").getValue().replace("%s", Pattern);
	}

	@Override
	protected boolean createSubtree(Book book) {
		return createBookWithAuthorsSubtree(book);
	}
}
