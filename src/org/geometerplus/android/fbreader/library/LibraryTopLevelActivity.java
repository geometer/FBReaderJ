/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.library;

import java.util.ArrayList;

import android.os.Bundle;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.tree.FBTree;

public class LibraryTopLevelActivity extends LibraryBaseActivity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		final ArrayList<FBTree> items = new ArrayList<FBTree>();
		items.add(new TopLevelTree(myResource.getResource("searchResults")));
		items.add(new TopLevelTree(myResource.getResource("recent")));
		items.add(new TopLevelTree(myResource.getResource("byAuthor")));
		items.add(new TopLevelTree(myResource.getResource("byTag")));
		items.add(new TopLevelTree(myResource.getResource("fileTree")));
		setListAdapter(new LibraryAdapter(items));
	}
}

class TopLevelTree extends FBTree {
	private final ZLResource myResource;

	public TopLevelTree(ZLResource resource) {
		myResource = resource;
	}

	@Override
	public String getName() {
		return myResource.getValue();
	}

	@Override
	public String getSummary() {
		return myResource.getResource("summary").getValue();
	}
}
