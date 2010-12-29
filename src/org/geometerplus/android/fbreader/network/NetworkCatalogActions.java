/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

import android.app.AlertDialog;
import android.os.Message;
import android.os.Handler;
import android.view.Menu;
import android.view.ContextMenu;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.PackageUtil;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkTreeFactory;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.tree.NetworkCatalogRootTree;
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

	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof NetworkCatalogTree;
	}

	@Override
	public String getTreeTitle(NetworkTree tree) {
		if (tree instanceof NetworkCatalogRootTree) {
			return tree.getName();
		}
		return tree.getName() + " - " + ((NetworkCatalogTree) tree).Item.Link.getSiteName();
	}

	@Override
	public void buildContextMenu(NetworkBaseActivity activity, ContextMenu menu, NetworkTree tree) {
		final NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
		final NetworkCatalogItem item = catalogTree.Item;
		menu.setHeaderTitle(tree.getName());

		final boolean isVisible = item.getVisibility() == ZLBoolean3.B3_TRUE;
		boolean hasItems = false;

		final String catalogUrl = item.URLByType.get(NetworkCatalogItem.URL_CATALOG);
		if (catalogUrl != null) {
			addMenuItem(menu, OPEN_CATALOG_ITEM_ID, "openCatalog");
			hasItems = true;
		}

		if (tree instanceof NetworkCatalogRootTree) {
			if (isVisible) {
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
			INetworkLink link = catalogTree.Item.Link; 
			if (link instanceof ICustomNetworkLink) {
				addMenuItem(menu, CUSTOM_CATALOG_EDIT, "editCustomCatalog");
				addMenuItem(menu, CUSTOM_CATALOG_REMOVE, "removeCustomCatalog");
			}
		} else {
			if (item.URLByType.get(NetworkCatalogItem.URL_HTML_PAGE) != null) {
				addMenuItem(menu, OPEN_IN_BROWSER_ITEM_ID, "openInBrowser");
				hasItems = true;
			}
		}

		if (!isVisible && !hasItems) {
			switch (item.Visibility) {
			case NetworkCatalogItem.VISIBLE_LOGGED_USER:
				if (item.Link.authenticationManager() != null) {
					addMenuItem(menu, SIGNIN_ITEM_ID, "signIn");
				}
				break;
			}
		}
	}

	@Override
	public int getDefaultActionCode(NetworkBaseActivity activity, NetworkTree tree) {
		final NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
		final NetworkCatalogItem item = catalogTree.Item;
		if (item.URLByType.get(NetworkCatalogItem.URL_CATALOG) != null) {
			return OPEN_CATALOG_ITEM_ID;
		}
		if (item.URLByType.get(NetworkCatalogItem.URL_HTML_PAGE) != null) {
			return OPEN_IN_BROWSER_ITEM_ID;
		}
		if (item.getVisibility() != ZLBoolean3.B3_TRUE) {
			switch (item.Visibility) {
			case NetworkCatalogItem.VISIBLE_LOGGED_USER:
				if (item.Link.authenticationManager() != null) {
					return SIGNIN_ITEM_ID;
				}
				break;
			}
			return TREE_NO_ACTION;
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
		return true;
	}

	@Override
	public boolean prepareOptionsMenu(NetworkBaseActivity activity, Menu menu, NetworkTree tree) {
		final NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
		final NetworkCatalogItem item = catalogTree.Item;

		final String catalogUrl = item.URLByType.get(NetworkCatalogItem.URL_CATALOG);
		final boolean isLoading = (catalogUrl != null) ?
			NetworkView.Instance().containsItemsLoadingRunnable(catalogUrl) : false;

		prepareOptionsItem(menu, RELOAD_ITEM_ID, catalogUrl != null && !isLoading);

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
		final NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
		if (catalogTree.Item.getVisibility() == ZLBoolean3.B3_TRUE) {
			return false;
		}
		switch (catalogTree.Item.Visibility) {
		case NetworkCatalogItem.VISIBLE_LOGGED_USER:
			NetworkDialog.show(activity, NetworkDialog.DIALOG_AUTHENTICATION, ((NetworkCatalogTree)tree).Item.Link, new Runnable() {
				public void run() {
					if (catalogTree.Item.getVisibility() != ZLBoolean3.B3_TRUE) {
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
		switch (actionCode) {
			case OPEN_CATALOG_ITEM_ID:
				doExpandCatalog(activity, (NetworkCatalogTree)tree);
				return true;
			case OPEN_IN_BROWSER_ITEM_ID:
				Util.openInBrowser(
					activity,
					((NetworkCatalogTree)tree).Item.URLByType.get(NetworkCatalogItem.URL_HTML_PAGE)
				);
				return true;
			case RELOAD_ITEM_ID:
				doReloadCatalog(activity, (NetworkCatalogTree)tree);
				return true;
			case SIGNIN_ITEM_ID:
				NetworkDialog.show(activity, NetworkDialog.DIALOG_AUTHENTICATION, ((NetworkCatalogTree)tree).Item.Link, null);
				return true;
			case SIGNUP_ITEM_ID:
				Util.runRegistrationDialog(activity, ((NetworkCatalogTree)tree).Item.Link);
				return true;
			case SIGNOUT_ITEM_ID:
				doSignOut(activity, (NetworkCatalogTree)tree);
				return true;
			case REFILL_ACCOUNT_ITEM_ID:
			{
				final RefillAccountActions actions = new RefillAccountActions();
				final NetworkTree refillTree = activity.getDefaultTree();
				final int refillActionCode = actions.getDefaultActionCode(activity, refillTree);
				if (refillActionCode == TREE_SHOW_CONTEXT_MENU) {
					activity.getListView().showContextMenu();
				} else if (refillActionCode >= 0) {
					actions.runAction(activity, refillTree, refillActionCode);
				}
				return true;
			}
			case CUSTOM_CATALOG_EDIT:
				NetworkDialog.show(activity, NetworkDialog.DIALOG_CUSTOM_CATALOG, ((NetworkCatalogTree)tree).Item.Link, null);
				return true;
			case CUSTOM_CATALOG_REMOVE:
				removeCustomLink((ICustomNetworkLink)((NetworkCatalogTree)tree).Item.Link);
				return true;
		}
		return false;
	}


	private static class ExpandCatalogHandler extends ItemsLoadingHandler {

		private final String myKey;
		private final NetworkCatalogTree myTree;

		ExpandCatalogHandler(NetworkCatalogTree tree, String key) {
			myTree = tree;
			myKey = key;
		}

		@Override
		public void onUpdateItems(List<NetworkLibraryItem> items) {
			for (NetworkLibraryItem item: items) {
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
				Set<NetworkLibraryItem> uncommitedItems) {
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
			final ZLResource dialogResource = ZLResource.resource("dialog");
			ZLResource boxResource = null;
			String msg = null;
			if (errorMessage != null) {
				boxResource = dialogResource.getResource("networkError");
				msg = errorMessage;
			} else if (childrenEmpty) {
				// TODO: make ListView's empty view instead
				boxResource = dialogResource.getResource("emptyCatalogBox");
				msg = boxResource.getResource("message").getValue();
			}
			if (msg != null) {
				if (NetworkView.Instance().isInitialized()) {
					final NetworkCatalogActivity activity = NetworkView.Instance().getOpenedActivity(myKey);
					if (activity != null) {
						final ZLResource buttonResource = dialogResource.getResource("button");
						new AlertDialog.Builder(activity)
							.setTitle(boxResource.getResource("title").getValue())
							.setMessage(msg)
							.setIcon(0)
							.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
							.create().show();
					}
				}
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

	public void doExpandCatalog(final NetworkBaseActivity activity, final NetworkCatalogTree tree) {
		final String url = tree.Item.URLByType.get(NetworkCatalogItem.URL_CATALOG);
		if (url == null) {
			throw new RuntimeException("That's impossible!!!");
		}
		NetworkView.Instance().tryResumeLoading(activity, tree, url, new Runnable() {
			public void run() {
				boolean resumeNotLoad = false;
				if (tree.hasChildren()) {
					if (tree.isContentValid()) {
						if (tree.Item.supportsResumeLoading()) {
							resumeNotLoad = true;
						} else {
							NetworkView.Instance().openTree(activity, tree, url);
							return;
						}
					} else {
						tree.ChildrenItems.clear();
						tree.clear();
						NetworkView.Instance().fireModelChangedAsync();
					}
				}

				final ExpandCatalogHandler handler = new ExpandCatalogHandler(tree, url);
				NetworkView.Instance().startItemsLoading(
					activity,
					url,
					new ExpandCatalogRunnable(handler, tree, true, resumeNotLoad)
				);
				processExtraData(activity, tree.Item.extraData(), new Runnable() {
					public void run() {
						NetworkView.Instance().openTree(activity, tree, url);
					}
				});
			}
		});
	}

	public void doReloadCatalog(NetworkBaseActivity activity, final NetworkCatalogTree tree) {
		final String url = tree.Item.URLByType.get(NetworkCatalogItem.URL_CATALOG);
		if (url == null) {
			throw new RuntimeException("That's impossible!!!");
		}
		if (NetworkView.Instance().containsItemsLoadingRunnable(url)) {
			return;
		}
		tree.ChildrenItems.clear();
		tree.clear();
		NetworkView.Instance().fireModelChangedAsync();
		final ExpandCatalogHandler handler = new ExpandCatalogHandler(tree, url);
		NetworkView.Instance().startItemsLoading(
			activity,
			url,
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
		library.updateChildren();
		library.synchronize();
		NetworkView.Instance().fireModelChangedAsync();
	}
}
