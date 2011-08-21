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

import java.util.Map;

import android.app.Activity;

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.NetworkURLCatalogItem;
import org.geometerplus.fbreader.network.opds.BasketItem;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

import org.geometerplus.android.fbreader.network.ItemsLoader;
import org.geometerplus.android.fbreader.network.ItemsLoadingService;
import org.geometerplus.android.fbreader.network.Util;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.PackageUtil;

public class OpenCatalogAction extends CatalogAction {
	public OpenCatalogAction(Activity activity) {
		super(activity, ActionCode.OPEN_CATALOG, "openCatalog");
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		if (!super.isVisible(tree)) {
			return false;
		}
		final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
		if (!(item instanceof NetworkURLCatalogItem)) {
			return true;
		}
		return ((NetworkURLCatalogItem)item).getUrl(UrlInfo.Type.Catalog) != null;
	}

	@Override
	public void run(NetworkTree tree) {
		final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
		if (item instanceof BasketItem && item.Link.basket().bookIds().size() == 0) {
			UIUtil.showErrorMessage(myActivity, "emptyBasket");
		} else {
			doExpandCatalog((NetworkCatalogTree)tree);
		}
	}

	private void tryResumeLoading(Activity activity, NetworkCatalogTree tree, Runnable expandRunnable) {
		final ItemsLoader runnable = ItemsLoadingService.getRunnable(tree);
		if (runnable != null && runnable.tryResumeLoading()) {
			Util.openTree(activity, tree);
			return;
		}
		if (runnable == null) {
			expandRunnable.run();
		} else {
			runnable.runOnFinish(expandRunnable);
		}
	}

	private void doExpandCatalog(final NetworkCatalogTree tree) {
		tryResumeLoading(myActivity, tree, new Runnable() {
			public void run() {
				boolean resumeNotLoad = false;
				if (tree.hasChildren()) {
					if (tree.isContentValid()) {
						if (tree.Item.supportsResumeLoading()) {
							resumeNotLoad = true;
						} else {
							Util.openTree(myActivity, tree);
							return;
						}
					} else {
						tree.clearCatalog();
					}
				}

				ItemsLoadingService.start(
					myActivity,
					tree,
					new CatalogExpander(myActivity, tree, true, resumeNotLoad)
				);
				processExtraData(tree.Item.extraData(), new Runnable() {
					public void run() {
						Util.openTree(myActivity, tree);
					}
				});
			}
		});
	}

	private void processExtraData(Map<String,String> extraData, final Runnable postRunnable) {
		if (extraData != null && !extraData.isEmpty()) {
			PackageUtil.runInstallPluginDialog(myActivity, extraData, postRunnable);
		} else {
			postRunnable.run();
		}
	}
}
