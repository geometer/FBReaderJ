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

abstract class FirstLevelTree extends LibraryTree {
	private final String myId;
	private final ZLResource myResource;

	FirstLevelTree(RootTree root, int position, String id) {
		super(root, position);
		myId = id;
		myResource = resource().getResource(myId);
	}

	FirstLevelTree(RootTree root, String id) {
		super(root);
		myId = id;
		myResource = resource().getResource(myId);
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
		return myId;
	}

	@Override
	public boolean isSelectable() {
		return false;
	}
}
