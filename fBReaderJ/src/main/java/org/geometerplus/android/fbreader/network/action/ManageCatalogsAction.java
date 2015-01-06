/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;

import org.geometerplus.fbreader.network.tree.RootTree;
import org.geometerplus.fbreader.network.tree.ManageCatalogsItemTree;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.network.CatalogManagerActivity;
import org.geometerplus.android.fbreader.network.NetworkLibraryActivity;

public class ManageCatalogsAction extends RootAction {
	public ManageCatalogsAction(Activity activity) {
		super(activity, ActionCode.MANAGE_CATALOGS, "manageCatalogs", R.drawable.ic_menu_filter);
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		return tree instanceof RootTree || tree instanceof ManageCatalogsItemTree;
	}

	@Override
	public void run(NetworkTree tree) {
		final NetworkLibrary library = NetworkLibrary.Instance();

		final ArrayList<String> ids = new ArrayList<String>(library.activeIds());
		final ArrayList<String> inactiveIds = new ArrayList<String>(library.allIds());
		inactiveIds.removeAll(ids);

		OrientationUtil.startActivityForResult(
			myActivity,
			new Intent(myActivity.getApplicationContext(), CatalogManagerActivity.class)
				.putStringArrayListExtra(NetworkLibraryActivity.ENABLED_CATALOG_IDS_KEY, ids)
				.putStringArrayListExtra(NetworkLibraryActivity.DISABLED_CATALOG_IDS_KEY, inactiveIds),
			NetworkLibraryActivity.REQUEST_MANAGE_CATALOGS
		);
	}
}
