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

import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.os.Message;
import android.os.Handler;
import android.view.ContextMenu;
import android.net.Uri;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;

import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkTreeFactory;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.tree.NetworkCatalogRootTree;
import org.geometerplus.fbreader.network.authentication.*;

import org.geometerplus.android.fbreader.ZLTreeAdapter;


class NetworkCatalogActions extends NetworkTreeActions {

	public static final int EXPAND_OR_COLLAPSE_TREE_ITEM_ID = 0;
	public static final int OPEN_IN_BROWSER_ITEM_ID = 1;
	public static final int RELOAD_ITEM_ID = 2;
	public static final int SIGNIN_ITEM_ID = 4;
	public static final int SIGNOUT_ITEM_ID = 5;
	public static final int REFILL_ACCOUNT_ITEM_ID = 6;
	public static final int STOP_LOADING_ITEM_ID = 7;


	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof NetworkCatalogTree;
	}

	@Override
	public void buildContextMenu(NetworkLibraryActivity activity, ContextMenu menu, NetworkTree tree) {
		final NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
		final NetworkCatalogItem item = catalogTree.Item;
		menu.setHeaderTitle(tree.getName());
		final String catalogUrl = item.URLByType.get(NetworkCatalogItem.URL_CATALOG);
		final boolean isOpened = tree.hasChildren() && activity.getAdapter().isOpen(tree);

		final ItemsLoadingRunnable catalogRunnable = (catalogUrl != null) ? 
			NetworkView.Instance().getItemsLoadingRunnable(catalogUrl) : null;
		final boolean isLoading = catalogRunnable != null;

		if (catalogUrl != null) {
			if (isLoading) {
				if (catalogRunnable.InterruptFlag.get()) {
					addMenuItem(menu, TREE_NO_ACTION, "stoppingCatalogLoading");
				} else {
					addMenuItem(menu, STOP_LOADING_ITEM_ID, "stopLoading");
				}
			} else {
				final String expandOrCollapseTitle;
				if (tree instanceof NetworkCatalogRootTree) {
					expandOrCollapseTitle = isOpened ? "closeCatalog" : "openCatalog";
				} else {
					expandOrCollapseTitle = isOpened ? "collapseTree" : "expandTree";
				}
				addMenuItem(menu, EXPAND_OR_COLLAPSE_TREE_ITEM_ID, expandOrCollapseTitle);
				if (isOpened) {
					addMenuItem(menu, RELOAD_ITEM_ID, "reload");
				}
			}
		}

		if (tree instanceof NetworkCatalogRootTree) {
			NetworkAuthenticationManager mgr = item.Link.authenticationManager();
			if (mgr != null) {
				final boolean maybeSignedIn = mgr.isAuthorised(false).Status != ZLBoolean3.B3_FALSE;
				if (maybeSignedIn) {
					addMenuItem(menu, SIGNOUT_ITEM_ID, "signOut", mgr.currentUserName());
					if (mgr.refillAccountLink() != null) {
						final String account = mgr.currentAccount();
						if (account != null) {
							addMenuItem(menu, REFILL_ACCOUNT_ITEM_ID, "refillAccount", account);
						}
					}
				} else {
					addMenuItem(menu, SIGNIN_ITEM_ID, "signIn");
					/*if (mgr.passwordRecoverySupported()) {
						//registerAction(new PasswordRecoveryAction(mgr), true);
					}*/
				}
			}
		} else {
			if (item.URLByType.get(NetworkCatalogItem.URL_HTML_PAGE) != null) {
				addMenuItem(menu, OPEN_IN_BROWSER_ITEM_ID, "openInBrowser");
			}
		}
	}

	@Override
	public int getDefaultActionCode(NetworkTree tree) {
		final NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
		final NetworkCatalogItem item = catalogTree.Item;
		if (item.URLByType.get(NetworkCatalogItem.URL_CATALOG) != null) {
			return EXPAND_OR_COLLAPSE_TREE_ITEM_ID;
		}
		if (item.URLByType.get(NetworkCatalogItem.URL_HTML_PAGE) != null) {
			return OPEN_IN_BROWSER_ITEM_ID;
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
	public boolean runAction(NetworkLibraryActivity activity, NetworkTree tree, int actionCode) {
		switch (actionCode) {
			case EXPAND_OR_COLLAPSE_TREE_ITEM_ID:
				doExpandCatalog(activity, (NetworkCatalogTree)tree);
				return true;
			case OPEN_IN_BROWSER_ITEM_ID:
				NetworkView.Instance().openInBrowser(
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
			case SIGNOUT_ITEM_ID:
				doSignOut(activity, (NetworkCatalogTree)tree);
				return true;
			case REFILL_ACCOUNT_ITEM_ID:
				NetworkView.Instance().openInBrowser(
					activity,
					((NetworkCatalogTree)tree).Item.Link.authenticationManager().refillAccountLink()
				);
				return true;
			case STOP_LOADING_ITEM_ID:
				doStopLoading((NetworkCatalogTree)tree);
				return true;
		}
		return false;
	}


	private static final LinkedList<NetworkTree> ourProcessingTrees = new LinkedList<NetworkTree>();

	private boolean startProcessingTree(NetworkTree tree) {
		if (ourProcessingTrees.contains(tree)) {
			return false;
		}
		ourProcessingTrees.add(tree);
		return true;
	}

	private void endProcessingTree(NetworkTree tree) {
		ourProcessingTrees.remove(tree);
	}

	private class ExpandCatalogHandler extends ItemsLoadingHandler {

		private final NetworkLibraryActivity myActivity; // TODO: this activity may become invalid
		private final NetworkCatalogTree myTree;
		private boolean myDoExpand;

		ExpandCatalogHandler(NetworkLibraryActivity activity, NetworkCatalogTree tree) {
			myActivity = activity;
			myTree = tree;
			myDoExpand = !myTree.hasChildren();
		}

		public void onUpdateItems(List<NetworkLibraryItem> items) {
			for (NetworkLibraryItem item: items) {
				myTree.ChildrenItems.add(item);
				NetworkTreeFactory.createNetworkTree(myTree, item);
			}
		}

		public void afterUpdateItems() {
			if (NetworkView.Instance().isInitialized()) {
				NetworkView.Instance().fireModelChanged();
			}
			if (myDoExpand) {
				ZLTreeAdapter adapter = myActivity.getAdapter();
				if (adapter != null) {
					adapter.expandOrCollapseTree(myTree);
				}
			}
			myDoExpand = !myTree.hasChildren();
		}

		public void onFinish(String errorMessage) {
			afterUpdateCatalog(errorMessage, myTree.ChildrenItems.size() == 0);
			endProcessingTree(myTree);
		}

		private void afterUpdateCatalog(String errorMessage, boolean childrenEmpty) {
			final ZLResource dialogResource = ZLResource.resource("dialog");
			ZLResource boxResource = null;
			String msg = null;
			if (errorMessage != null) {
				boxResource = dialogResource.getResource("networkError");
				msg = errorMessage;
			} else if (childrenEmpty) {
				boxResource = dialogResource.getResource("emptyCatalogBox");
				msg = boxResource.getResource("message").getValue();
			}
			if (msg != null) {
				final ZLResource buttonResource = dialogResource.getResource("button");
				new AlertDialog.Builder(myActivity)
					.setTitle(boxResource.getResource("title").getValue())
					.setMessage(msg)
					.setIcon(0)
					.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
					.create().show();
			}
			final NetworkLibrary library = NetworkLibrary.Instance();
			library.invalidateAccountDependents();
			library.synchronize();
			if (NetworkView.Instance().isInitialized()) {
				NetworkView.Instance().fireModelChanged();
			}
		}
	}

	private static class ExpandCatalogRunnable extends ItemsLoadingRunnable {

		private final NetworkCatalogTree myTree;
		private final boolean myCheckAuthentication;

		public ExpandCatalogRunnable(ItemsLoadingHandler handler, NetworkCatalogTree tree, boolean checkAuthentication) {
			super(handler, CATALOG_LOADING);
			myTree = tree;
			myCheckAuthentication = checkAuthentication;
		}

		public int getNotificationId() {
			return NetworkNotifications.Instance().getCatalogLoadingId();
		}

		public String getResourceKey() {
			return "downloadingCatalogs";
		}

		public String doBefore() {
			/*if (!NetworkOperationRunnable::tryConnect()) {
				return;
			}*/
			final NetworkLink link = myTree.Item.Link;
			if (myCheckAuthentication && link.authenticationManager() != null) {
				NetworkAuthenticationManager mgr = link.authenticationManager();
				AuthenticationStatus auth = mgr.isAuthorised(true);
				if (auth.Message != null) {
					return auth.Message;
				}
				if (auth.Status == ZLBoolean3.B3_TRUE && mgr.needsInitialization()) {
					final String err = mgr.initialize();
					if (err != null) {
						mgr.logOut();
					}
				}
			}
			return null;
		}

		public String doLoading(NetworkOperationData.OnNewItemListener doWithListener) {
			return myTree.Item.loadChildren(doWithListener);
		}
	}

	public void doExpandCatalog(NetworkLibraryActivity activity, final NetworkCatalogTree tree) {
		if (tree.hasChildren()) {
			activity.getAdapter().expandOrCollapseTree(tree);
			return;
		}
		if (!startProcessingTree(tree)) {
			return;
		}
		final String url = tree.Item.URLByType.get(NetworkCatalogItem.URL_CATALOG);
		if (url == null) {
			throw new RuntimeException("That's impossible!!!");
		}
		final ExpandCatalogHandler handler = new ExpandCatalogHandler(activity, tree);
		NetworkView.Instance().startItemsLoading(
			activity,
			url,
			new ExpandCatalogRunnable(handler, tree, true)
		);
	}

	public void doReloadCatalog(NetworkLibraryActivity activity, final NetworkCatalogTree tree) {
		if (!startProcessingTree(tree)) {
			return;
		}
		final String url = tree.Item.URLByType.get(NetworkCatalogItem.URL_CATALOG);
		if (url == null) {
			throw new RuntimeException("That's impossible!!!");
		}
		activity.getAdapter().expandOrCollapseTree(tree);
		tree.ChildrenItems.clear();
		tree.clear();
		activity.getAdapter().resetTree();
		final ExpandCatalogHandler handler = new ExpandCatalogHandler(activity, tree);
		NetworkView.Instance().startItemsLoading(
			activity,
			url,
			new ExpandCatalogRunnable(handler, tree, false)
		);
	}

	private void doStopLoading(NetworkCatalogTree tree) {
		final String url = tree.Item.URLByType.get(NetworkCatalogItem.URL_CATALOG);
		if (url == null) {
			throw new RuntimeException("That's impossible!!!");
		}
		final ItemsLoadingRunnable runnable = NetworkView.Instance().getItemsLoadingRunnable(url);
		if (runnable != null) {
			runnable.InterruptFlag.set(true);
		}
	}

	private void doSignOut(NetworkLibraryActivity activity, NetworkCatalogTree tree) {
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				final NetworkLibrary library = NetworkLibrary.Instance();
				library.invalidateAccountDependents();
				library.synchronize();
				if (NetworkView.Instance().isInitialized()) {
					NetworkView.Instance().fireModelChanged();
				}
			}
		};
		final NetworkAuthenticationManager mgr = tree.Item.Link.authenticationManager();
		final Runnable runnable = new Runnable() {
			public void run() {
				if (mgr.isAuthorised(false).Status != ZLBoolean3.B3_FALSE) {
					mgr.logOut();
					handler.sendEmptyMessage(-1);
				}
			}
		};
		((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("signOut", runnable, activity);
	}
}
