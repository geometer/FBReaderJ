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

package org.geometerplus.android.fbreader.network;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.network.NetworkLibrary;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.api.PluginApi;

public class AddCatalogMenuActivity extends MenuActivity {
	private final ZLResource myResource = NetworkLibrary.resource().getResource("addCatalog");

	private void addItem(String id, int weight) {
		myInfos.add(new PluginApi.MenuActionInfo(
			Uri.parse("http://data.fbreader.org/add_catalog/" + id),
			myResource.getResource(id).getValue(),
			weight
		));
	}

	@Override
	protected void init() {
		setTitle(myResource.getResource("title").getValue());
		addItem("editUrl", 1);
		//addItem("scanLocalNetwork", 2);
	}

	@Override
	protected String getAction() {
		return Util.ADD_CATALOG_ACTION;
	}

	@Override
	protected void runItem(final PluginApi.MenuActionInfo info) {
		try {
			startActivity(
				new Intent(getAction()).addCategory(Intent.CATEGORY_DEFAULT).setData(info.getId())
			);
		} catch (ActivityNotFoundException e) {
		}
		finish();
	}
}
