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

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.NetworkURLCatalogItem;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

import org.geometerplus.android.fbreader.network.ItemsLoadingService;

public class ReloadCatalogAction extends CatalogAction {
	public ReloadCatalogAction(Activity activity) {
		super(activity, ActionCode.RELOAD_CATALOG, "reload");
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		if (!super.isVisible(tree)) {
			return false;
		}
		final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
		if (!(item instanceof NetworkURLCatalogItem)) {
			return false;
		}
		return
			((NetworkURLCatalogItem)item).getUrl(UrlInfo.Type.Catalog) != null &&
			ItemsLoadingService.getRunnable(tree) == null;
	}

	@Override
	protected void run(NetworkTree tree) {
		if (ItemsLoadingService.getRunnable(tree) != null) {
			return;
		}
		((NetworkCatalogTree)tree).clearCatalog();
		ItemsLoadingService.start(
			myActivity,
			tree,
			new CatalogExpander(myActivity, (NetworkCatalogTree)tree, false, false)
		);
	}
}
