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

package org.geometerplus.android.fbreader.network.action;

import android.app.Activity;

import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;

import org.geometerplus.android.fbreader.network.Util;

public abstract class Action {
	public final int Code;
	public final int IconId;

	protected final Activity myActivity;
	private final String myResourceKey;

	protected Action(Activity activity, int code, String resourceKey, int iconId) {
		myActivity = activity;
		Code = code;
		myResourceKey = resourceKey;
		IconId = iconId;
	}

	public abstract boolean isVisible(NetworkTree tree);

	public boolean isEnabled(NetworkTree tree) {
		return true;
	}

	protected abstract void run(NetworkTree tree);

	public String getContextLabel(NetworkTree tree) {
		return
			NetworkLibrary.resource().getResource(myResourceKey).getValue();
	}

	public String getOptionsLabel(NetworkTree tree) {
		return
			NetworkLibrary.resource().getResource("menu").getResource(myResourceKey).getValue();
	}

	public void checkAndRun(final NetworkTree tree) {
		if (tree instanceof NetworkCatalogTree) {
			final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
			switch (item.getVisibility()) {
				case B3_TRUE:
					run(tree);
					break;
				case B3_UNDEFINED:
					Util.runAuthenticationDialog(myActivity, item.Link, new Runnable() {
						public void run() {
							if (item.getVisibility() != ZLBoolean3.B3_TRUE) {
								return;
							}
							if (Code != ActionCode.SIGNIN) {
								Action.this.run(tree);
							}
						}
					});
					break;
			}
		} else {
			run(tree);
		}
	}
}
