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

public class FavoritesTree extends FilteredTree {
	private final ZLResource myResource;

	FavoritesTree(RootTree root) {
		super(root, new Filter.ByLabel(Book.FAVORITE_LABEL), -1);
		myResource = resource().getResource(ROOT_FAVORITES);
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
	public String getSummary() {
		return myResource.getResource("summary").getValue();
	}

	@Override
	protected String getStringId() {
		return ROOT_FAVORITES;
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	@Override
	public Status getOpeningStatus() {
		return Collection.hasBooks(new Filter.ByLabel(Book.FAVORITE_LABEL))
			? Status.ALWAYS_RELOAD_BEFORE_OPENING
			: Status.CANNOT_OPEN;
	}

	@Override
	public String getOpeningStatusMessage() {
		return getOpeningStatus() == Status.CANNOT_OPEN
			? "noFavorites" : super.getOpeningStatusMessage();
	}

	@Override
	protected boolean createSubtree(Book book) {
		return createBookWithAuthorsSubtree(book);
	}
}
