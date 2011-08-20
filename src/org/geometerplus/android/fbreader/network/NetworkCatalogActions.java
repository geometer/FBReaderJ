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

package org.geometerplus.android.fbreader.network;

import java.util.*;

import android.app.Activity;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.android.util.UIUtil;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.*;
import org.geometerplus.fbreader.network.tree.NetworkTreeFactory;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.opds.BasketItem;

import org.geometerplus.android.fbreader.network.action.ActionCode;

public class NetworkCatalogActions {
	public boolean runAction(final NetworkLibraryActivity activity, NetworkTree tree, int actionCode) {
		final NetworkCatalogTree catalogTree = (NetworkCatalogTree)tree;

		final NetworkCatalogItem item = catalogTree.Item;
		switch (actionCode) {
			case ActionCode.BASKET_CLEAR:
				item.Link.basket().clear();
				return true;
			case ActionCode.BASKET_BUY_ALL_BOOKS:
				return true;
		}
		return false;
	}

	public static class CatalogExpander extends ItemsLoader {
		private final NetworkCatalogTree myTree;
		private final boolean myCheckAuthentication;
		private final boolean myResumeNotLoad;

		public CatalogExpander(Activity activity,
				NetworkCatalogTree tree, boolean checkAuthentication, boolean resumeNotLoad) {
			super(activity);
			myTree = tree;
			myCheckAuthentication = checkAuthentication;
			myResumeNotLoad = resumeNotLoad;
		}

		@Override
		public void doBefore() throws ZLNetworkException {
			final INetworkLink link = myTree.Item.Link;
			if (myCheckAuthentication && link.authenticationManager() != null) {
				final NetworkAuthenticationManager mgr = link.authenticationManager();
				try {
					if (mgr.isAuthorised(true) && mgr.needsInitialization()) {
						mgr.initialize();
					}
				} catch (ZLNetworkException e) {
					mgr.logOut();
				}
			}
		}

		@Override
		public void doLoading(NetworkOperationData.OnNewItemListener doWithListener) throws ZLNetworkException {
			if (myResumeNotLoad) {
				myTree.Item.resumeLoading(doWithListener);
			} else {
				myTree.Item.loadChildren(doWithListener);
			}
		}

		@Override
		protected void updateItems(List<NetworkItem> items) {
			for (NetworkItem item: items) {
				myTree.ChildrenItems.add(item);
				NetworkTreeFactory.createNetworkTree(myTree, item);
			}
			NetworkView.Instance().fireModelChanged();
		}

		@Override
		protected void onFinish(String errorMessage, boolean interrupted,
				Set<NetworkItem> uncommitedItems) {
			if (interrupted &&
					(!myTree.Item.supportsResumeLoading() || errorMessage != null)) {
				myTree.ChildrenItems.clear();
				myTree.clear();
			} else {
				myTree.removeItems(uncommitedItems);
				myTree.updateLoadedTime();
				if (!interrupted) {
					afterUpdateCatalog(errorMessage, myTree.ChildrenItems.size() == 0);
				}
				final NetworkLibrary library = NetworkLibrary.Instance();
				library.invalidateVisibility();
				library.synchronize();
			}
			NetworkView.Instance().fireModelChanged();
		}

		private void afterUpdateCatalog(String errorMessage, boolean childrenEmpty) {
			final NetworkLibraryActivity activity = NetworkLibraryActivity.getByTree(myTree);
			if (activity == null) {
				return;
			}
			if (errorMessage != null) {
				UIUtil.showMessageText(activity, errorMessage);
			} else if (childrenEmpty) {
				UIUtil.showErrorMessage(activity, "emptyCatalog");
			}
		}
	}

	public static void clearTree(Activity activity, final NetworkCatalogTree tree) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				tree.ChildrenItems.clear();
				tree.clear();
				NetworkView.Instance().fireModelChanged();
			}
		});
	}

	public static void doSignOut(final Activity activity, NetworkCatalogTree tree) {
		final NetworkAuthenticationManager mgr = tree.Item.Link.authenticationManager();
		final Runnable runnable = new Runnable() {
			public void run() {
				if (mgr.mayBeAuthorised(false)) {
					mgr.logOut();
					activity.runOnUiThread(new Runnable() {
						public void run() {
							final NetworkLibrary library = NetworkLibrary.Instance();
							library.invalidateVisibility();
							library.synchronize();
							NetworkView.Instance().fireModelChanged();
						}
					});
				}
			}
		};
		UIUtil.wait("signOut", runnable, activity);
	}
}
