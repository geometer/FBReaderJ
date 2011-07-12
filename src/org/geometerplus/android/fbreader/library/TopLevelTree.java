/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.tree.FBTree;

import org.geometerplus.android.fbreader.tree.ZLAndroidTree;

class TopLevelTree extends FBTree implements ZLAndroidTree {
	private final ZLResource myResource;
	private final String myParameter;
	private final int myCoverResourceId;
	private final Runnable myAction;

	public TopLevelTree(ZLResource resource, String parameter, int coverResourceId, Runnable action) {
		myResource = resource;
		myParameter = parameter;
		myCoverResourceId = coverResourceId;
		myAction = action;
	}

	public TopLevelTree(ZLResource resource, int coverResourceId, Runnable action) {
		this(resource, null, coverResourceId, action);
	}

	@Override
	public String getName() {
		return myResource.getValue();
	}

	@Override
	public String getSummary() {
		final String summary = myResource.getResource("summary").getValue();
		return myParameter == null ? summary : summary.replace("%s", myParameter);
	}

	public int getCoverResourceId() {
		return myCoverResourceId;
	}

	public void run() {
		myAction.run();
	}
}
