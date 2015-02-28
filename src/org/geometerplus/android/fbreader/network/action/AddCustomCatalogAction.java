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
import android.content.Intent;
import android.net.Uri;

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.tree.RootTree;
import org.geometerplus.fbreader.network.tree.AddCustomCatalogItemTree;

import org.geometerplus.android.fbreader.network.AddCatalogMenuActivity;

import org.geometerplus.zlibrary.ui.android.R;

public class AddCustomCatalogAction extends Action {
	public AddCustomCatalogAction(Activity activity) {
		super(activity, ActionCode.CUSTOM_CATALOG_ADD, "addCustomCatalog", R.drawable.ic_menu_add);
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		return tree instanceof RootTree || tree instanceof AddCustomCatalogItemTree;
	}

	@Override
	public void run(NetworkTree tree) {
		myActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://data.fbreader.org/add_catalog"), myActivity, AddCatalogMenuActivity.class));
	}
}
