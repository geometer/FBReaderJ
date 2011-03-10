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
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.os.Handler;
import android.view.Menu;
import android.view.ContextMenu;

import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.PackageUtil;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkTreeFactory;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.tree.NetworkCatalogRootTree;
import org.geometerplus.fbreader.network.opds.BasketItem;
import org.geometerplus.fbreader.network.authentication.*;


class NetworkCatalogActions extends NetworkTreeActions {
	public static final int OPEN_CATALOG_ITEM_ID = 0;
	public static final int OPEN_IN_BROWSER_ITEM_ID = 1;
	public static final int RELOAD_ITEM_ID = 2;
	public static final int SIGNUP_ITEM_ID = 3;
	public static final int SIGNIN_ITEM_ID = 4;
	public static final int SIGNOUT_ITEM_ID = 5;
	public static final int REFILL_ACCOUNT_ITEM_ID = 6;

	public static final int CUSTOM_CATALOG_EDIT = 7;
	public static final int CUSTOM_CATALOG_REMOVE = 8;

	public static final int BASKET_CLEAR = 9;
	public static final int BASKET_BUY_ALL_BOOKS = 10;

	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof NetworkCatalogTree;
	}

	@Override
	public String getTreeTitle(NetworkTree tree) {
		if (tree instanceof NetworkCatalogRootTree) {
			return tree.getName();
		}
		return tree.getName() + " - " + ((NetworkCatalogTree)tree).Item.Link.getSiteName();
	}

	@Override
	public void buildContextMenu(Activity activity, ContextMenu menu, NetworkTree tree) {
		final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
		final NetworkURLCatalogItem urlItem =
			item instanceof NetworkURLCatalogItem ? (NetworkURLCatalogItem)item : null;
		menu.setHeaderTitle(tree.getName());

		boolean hasItems = false;

		final String catalogUrl =
			urlItem != null ? urlItem.URLByType.get(NetworkURLCatalogItem.URL_CATALOG) : null;
		if (catalogUrl != null &&
			(!(item instanceof BasketItem) || item.Link.basket().bookIds().size() > 0)) {
			addMenuItem(menu, OPEN_CATALOG_ITEM_ID, "openCatalog");
			hasItems = true;
		}

		if (tree instanceof NetworkCatalogRootTree) {
			if (item.getVisibility() == ZLBoolean3.B3_TRUE) {
				final NetworkAuthenticationManager mgr = item.Link.authenticationManager();
				if (mgr != null) {
					if (mgr.mayBeAuthorised(false)) {
						addMenuItem(menu, SIGNOUT_ITEM_ID, "signOut", mgr.currentUserName());
						if (mgr.refillAccountLink() != null) {
							final String account = mgr.currentAccount();
							if (account != null) {
								addMenuItem(menu, REFILL_ACCOUNT_ITEM_ID, "refillAccount", account);
							}
						}
					} else {
						addMenuItem(menu, SIGNIN_ITEM_ID, "signIn");
						//if (mgr.passwordRecoverySupported()) {
						//	registerAction(new PasswordRecoveryAction(mgr), true);
						//}
					}
				}
			}
			INetworkLink link = item.Link; 
			if (link instanceof ICustomNetworkLink) {
				addMenuItem(menu, CUSTOM_CATALOG_EDIT, "editCustomCatalog");
				addMenuItem(menu, CUSTOM_CATALOG_REMOVE, "removeCustomCatalog");
			}
		} else {
			if (urlItem != null && urlItem.URLByType.get(NetworkURLCatalogItem.URL_HTML_PAGE) != null) {
				addMenuItem(menu, OPEN_IN_BROWSER_ITEM_ID, "openInBrowser");
				hasItems = true;
			}
		}

		if (item.getVisibility() == ZLBoolean3.B3_UNDEFINED &&
			!hasItems && item.Link.authenticationManager() != null) {
			addMenuItem(menu, SIGNIN_ITEM_ID, "signIn");
		}
	}

	@Override
	public int getDefaultActionCode(NetworkBaseActivity activity, NetworkTree tree) {
		final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
		if (!(item instanceof NetworkURLCatalogItem)) {
			return OPEN_CATALOG_ITEM_ID;
		}
		final NetworkURLCatalogItem urlItem = (NetworkURLCatalogItem)item;
		if (urlItem.URLByType.get(NetworkURLCatalogItem.URL_CATALOG) != null) {
			return OPEN_CATALOG_ITEM_ID;
		}
		if (urlItem.URLByType.get(NetworkURLCatalogItem.URL_HTML_PAGE) != null) {
			return OPEN_IN_BROWSER_ITEM_ID;
		}
		if (urlItem.getVisibility() == ZLBoolean3.B3_UNDEFINED &&
			urlItem.Link.authenticationManager() != null) {
			return SIGNIN_ITEM_ID;
		}
		return TREE_NO_ACTION;
	}

	@Override
	public String getConfirmText(NetworkTree tree, int actionCode) {
		if (actionCode == OPEN_IN_BROWSER_ITEM_ID) {
			return getConfirmValue("openInBrowser");
		}
		return null;
	}

	@Override
	public boolean createOptionsMenu(Menu menu, NetworkTree tree) {
		addOptionsItem(menu, RELOAD_ITEM_ID, "reload");
		addOptionsItem(menu, SIGNIN_ITEM_ID, "signIn");
		addOptionsItem(menu, SIGNUP_ITEM_ID, "signUp");
		addOptionsItem(menu, SIGNOUT_ITEM_ID, "signOut", "");
		addOptionsItem(menu, REFILL_ACCOUNT_ITEM_ID, "refillAccount");
		if (((NetworkCatalogTree)tree).Item instanceof BasketItem) {
			addOptionsItem(menu, BASKET_CLEAR, "clearBasket");
			addOptionsItem(menu, BASKET_BUY_ALL_BOOKS, "buyAllBooks");
		}
		return true;
	}

	@Override
	public boolean prepareOptionsMenu(NetworkBaseActivity activity, Menu menu, NetworkTree tree) {
		final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
		final NetworkURLCatalogItem urlItem =
			item instanceof NetworkURLCatalogItem ? (NetworkURLCatalogItem)item : null;

		prepareOptionsItem(menu, RELOAD_ITEM_ID,
				urlItem != null &&
				urlItem.URLByType.get(NetworkURLCatalogItem.URL_CATALOG) != null &&
				!NetworkView.Instance().containsItemsLoadingRunnable(tree.getUniqueKey()));

		boolean signIn = false;
		boolean signOut = false;
		boolean refill = false;
		String userName = null;
		String account = null;
		NetworkAuthenticationManager mgr = item.Link.authenticationManager();
		if (mgr != null) {
			if (mgr.mayBeAuthorised(false)) {
				userName = mgr.currentUserName();
				signOut = true;
				account = mgr.currentAccount();
				if (mgr.refillAccountLink() != null && account != null) {
					refill = true;
				}
			} else {
				signIn = true;
				//if (mgr.passwordRecoverySupported()) {
				//	registerAction(new PasswordRecoveryAction(mgr), true);
				//}
			}
		}
		prepareOptionsItem(menu, SIGNIN_ITEM_ID, signIn);
		prepareOptionsItem(menu, SIGNUP_ITEM_ID, signIn & Util.isRegistrationSupported(activity, item.Link));
		prepareOptionsItem(menu, SIGNOUT_ITEM_ID, signOut, "signOut", userName);
		prepareOptionsItem(menu, REFILL_ACCOUNT_ITEM_ID, refill);
		return true;
	}

	private boolean consumeByVisibility(final NetworkBaseActivity activity, final NetworkTree tree, final int actionCode) {
		final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
		switch (item.getVisibility()) {
			case B3_TRUE:
				return false;
			case B3_UNDEFINED:
				AuthenticationDialog.show(activity, ((NetworkCatalogTree)tree).Item.Link, new Runnable() {
					public void run() {
						if (item.getVisibility() != ZLBoolean3.B3_TRUE) {
							return;
						}
						if (actionCode != SIGNIN_ITEM_ID) {
							runAction(activity, tree, actionCode);
						}
					}
				});
				break;
		}
		return true;
	}

	@Override
	public boolean runAction(NetworkBaseActivity activity, NetworkTree tree, int actionCode) {
		if (consumeByVisibility(activity, tree, actionCode)) {
			return true;
		}

		final NetworkCatalogTree catalogTree = (NetworkCatalogTree)tree;
		final NetworkCatalogItem item = catalogTree.Item;
		switch (actionCode) {
			case OPEN_CATALOG_ITEM_ID:
			{
				if (item instanceof BasketItem && item.Link.basket().bookIds().size() == 0) {
					UIUtil.showErrorMessage(activity, "emptyBasket");
				} else {
					doExpandCatalog(activity, catalogTree);
				}
				return true;
			}
			case OPEN_IN_BROWSER_ITEM_ID:
				if (item instanceof NetworkURLCatalogItem) {
					Util.openInBrowser(
						activity,
						((NetworkURLCatalogItem)item).URLByType.get(NetworkURLCatalogItem.URL_HTML_PAGE)
					);
				}
				return true;
			case RELOAD_ITEM_ID:
				doReloadCatalog(activity, catalogTree);
				return true;
			case SIGNIN_ITEM_ID:
				AuthenticationDialog.show(activity, item.Link, null);
				return true;
			case SIGNUP_ITEM_ID:
				Util.runRegistrationDialog(activity, item.Link);
				return true;
			case SIGNOUT_ITEM_ID:
				doSignOut(activity, catalogTree);
				return true;
			case REFILL_ACCOUNT_ITEM_ID:
				new RefillAccountActions().runStandalone(activity, item.Link);
				return true;
			case CUSTOM_CATALOG_EDIT:
			{
				final Intent intent = new Intent(activity, AddCustomCatalogActivity.class);
				NetworkLibraryActivity.addLinkToIntent(
					intent,
					(ICustomNetworkLink)item.Link
				);
				activity.startActivity(intent);
				return true;
			}
			case CUSTOM_CATALOG_REMOVE:
				removeCustomLink((ICustomNetworkLink)item.Link);
				return true;
			case BASKET_CLEAR:
				item.Link.basket().clear();
				return true;
			case BASKET_BUY_ALL_BOOKS:
				return true;
		}
		return false;
	}


	private static class ExpandCatalogHandler extends ItemsLoadingHandler {
		private final NetworkTree.Key myKey;
		private final NetworkCatalogTree myTree;

		ExpandCatalogHandler(NetworkCatalogTree tree, NetworkTree.Key key) {
			myTree = tree;
			myKey = key;
		}

		@Override
		public void onUpdateItems(List<NetworkItem> items) {
			for (NetworkItem item: items) {
				myTree.ChildrenItems.add(item);
				NetworkTreeFactory.createNetworkTree(myTree, item);
			}
		}

		@Override
		public void afterUpdateItems() {
			if (NetworkView.Instance().isInitialized()) {
				NetworkView.Instance().fireModelChangedAsync();
			}
		}

		@Override
		public void onFinish(String errorMessage, boolean interrupted,
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
			if (NetworkView.Instance().isInitialized()) {
				NetworkView.Instance().fireModelChangedAsync();
			}
		}

		private void afterUpdateCatalog(String errorMessage, boolean childrenEmpty) {
			if (!NetworkView.Instance().isInitialized()) {
				return;
			}
			final NetworkCatalogActivity activity = NetworkView.Instance().getOpenedActivity(myKey);
			if (activity == null) {
				return;
			}
			String msg = null;
			if (errorMessage != null) {
				UIUtil.showErrorMessageText(activity, errorMessage);
			} else if (childrenEmpty) {
				UIUtil.showErrorMessage(activity, "emptyCatalog");
			}
		}
	}

	private static class ExpandCatalogRunnable extends ItemsLoadingRunnable {
		private final NetworkCatalogTree myTree;
		private final boolean myCheckAuthentication;
		private final boolean myResumeNotLoad;

		public ExpandCatalogRunnable(ItemsLoadingHandler handler,
				NetworkCatalogTree tree, boolean checkAuthentication, boolean resumeNotLoad) {
			super(handler);
			myTree = tree;
			myCheckAuthentication = checkAuthentication;
			myResumeNotLoad = resumeNotLoad;
		}

		public String getResourceKey() {
			return "downloadingCatalogs";
		}

		@Override
		public void doBefore() throws ZLNetworkException {
			/*if (!NetworkOperationRunnable::tryConnect()) {
				return;
			}*/
			final INetworkLink link = myTree.Item.Link;
			if (myCheckAuthentication && link.authenticationManager() != null) {
				final NetworkAuthenticationManager mgr = link.authenticationManager();
				if (mgr.isAuthorised(true) && mgr.needsInitialization()) {
					try {
						mgr.initialize();
					} catch (ZLNetworkException e) {
						mgr.logOut();
					}
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
	}

	private void processExtraData(final NetworkBaseActivity activity, Map<String,String> extraData, final Runnable postRunnable) {
		if (extraData != null && !extraData.isEmpty()) {
			PackageUtil.runInstallPluginDialog(activity, extraData, postRunnable);
		} else {
			postRunnable.run();
		}
	}

	private void doExpandCatalog(final NetworkBaseActivity activity, final NetworkCatalogTree tree) {
		final NetworkTree.Key key = tree.getUniqueKey();
		NetworkView.Instance().tryResumeLoading(activity, tree, new Runnable() {
			public void run() {
				boolean resumeNotLoad = false;
				if (tree.hasChildren()) {
					if (tree.isContentValid()) {
						if (tree.Item.supportsResumeLoading()) {
							resumeNotLoad = true;
						} else {
							Util.openTree(activity, tree);
							return;
						}
					} else {
						tree.ChildrenItems.clear();
						tree.clear();
						NetworkView.Instance().fireModelChangedAsync();
					}
				}

				/* FIXME: if catalog's loading will be very fast
				 * then it is possible that loading message is lost
				 * (see ExpandCatalogHandler.afterUpdateCatalog method).
				 * 
				 * For example, this can be fixed via adding method
				 * NetworkView.postCatalogLoadingResult, that will do the following:
				 * 1) If there is activity, then show message
				 * 2) If there is no activity, then save message, and show when activity is created
				 * 3) Remove unused messages (say, by timeout)
				 */
				final ExpandCatalogHandler handler = new ExpandCatalogHandler(tree, key);
				NetworkView.Instance().startItemsLoading(
					activity,
					key,
					new ExpandCatalogRunnable(handler, tree, true, resumeNotLoad)
				);
				processExtraData(activity, tree.Item.extraData(), new Runnable() {
					public void run() {
						Util.openTree(activity, tree);
					}
				});
			}
		});
	}

	public void doReloadCatalog(NetworkBaseActivity activity, final NetworkCatalogTree tree) {
		final NetworkTree.Key key = tree.getUniqueKey();
		if (NetworkView.Instance().containsItemsLoadingRunnable(key)) {
			return;
		}
		tree.ChildrenItems.clear();
		tree.clear();
		NetworkView.Instance().fireModelChangedAsync();
		final ExpandCatalogHandler handler = new ExpandCatalogHandler(tree, key);
		NetworkView.Instance().startItemsLoading(
			activity,
			key,
			new ExpandCatalogRunnable(handler, tree, false, false)
		);
	}

	private void doSignOut(NetworkBaseActivity activity, NetworkCatalogTree tree) {
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				final NetworkLibrary library = NetworkLibrary.Instance();
				library.invalidateVisibility();
				library.synchronize();
				if (NetworkView.Instance().isInitialized()) {
					NetworkView.Instance().fireModelChanged();
				}
			}
		};
		final NetworkAuthenticationManager mgr = tree.Item.Link.authenticationManager();
		final Runnable runnable = new Runnable() {
			public void run() {
				if (mgr.mayBeAuthorised(false)) {
					mgr.logOut();
					handler.sendEmptyMessage(0);
				}
			}
		};
		UIUtil.wait("signOut", runnable, activity);
	}

	private void removeCustomLink(ICustomNetworkLink link) {
		final NetworkLibrary library = NetworkLibrary.Instance();
		library.removeCustomLink(link);
		library.synchronize();
		NetworkView.Instance().fireModelChangedAsync();
	}
}
