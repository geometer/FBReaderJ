/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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
import android.os.Bundle;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.tree.SearchCatalogTree;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.fbreader.network.NetworkLibraryActivity;
import org.geometerplus.android.fbreader.network.NetworkSearchActivity;

import org.geometerplus.android.util.DeviceType;
import org.geometerplus.android.util.SearchDialogUtil;

public class RunSearchAction extends Action {
	public static SearchCatalogTree getSearchTree(FBTree tree) {
		for (; tree != null; tree = tree.Parent) {
			for (FBTree t : tree.subtrees()) {
				if (t instanceof SearchCatalogTree) {
					return (SearchCatalogTree)t;
				}
			}
		}
		return null;
	}

	private final boolean myFromContextMenu;

	public RunSearchAction(Activity activity, boolean fromContextMenu) {
		super(activity, ActionCode.SEARCH, "networkSearch", R.drawable.ic_menu_search);
		myFromContextMenu = fromContextMenu;
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		if (myFromContextMenu) {
			return tree instanceof SearchCatalogTree;
		} else {
			return getSearchTree(tree) != null;
		}
	}

	@Override
	public boolean isEnabled(NetworkTree tree) {
		return myLibrary.getStoredLoader(getSearchTree(tree)) == null;
	}

	@Override
	public void run(NetworkTree tree) {
		final Bundle bundle = new Bundle();
		bundle.putSerializable(
			NetworkLibraryActivity.TREE_KEY_KEY,
			getSearchTree(tree).getUniqueKey()
		);
		final NetworkLibrary library = myLibrary;
		if (DeviceType.Instance().hasStandardSearchDialog()) {
			myActivity.startSearch(library.NetworkSearchPatternOption.getValue(), true, bundle, false);
		} else {
			SearchDialogUtil.showDialog(myActivity, NetworkSearchActivity.class, library.NetworkSearchPatternOption.getValue(), null, bundle);
		}
	}
}
