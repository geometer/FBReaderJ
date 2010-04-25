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
	//public static final int DONT_SHOW_ITEM_ID = 3;
	public static final int SIGNIN_ITEM_ID = 4;
	public static final int SIGNOUT_ITEM_ID = 5;
	public static final int REFILL_ACCOUNT_ITEM_ID = 6;

	//public static final int DBG_UNLOAD_CATALOG_ITEM_ID = 128;

	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof NetworkCatalogTree;
	}

	@Override
	public void buildContextMenu(ContextMenu menu, NetworkTree tree) {
		final NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
		final NetworkCatalogItem item = catalogTree.Item;
		menu.setHeaderTitle(tree.getName());
		final boolean isOpened = tree.hasChildren() && NetworkLibraryActivity.Instance.getAdapter().isOpen(tree);
		if (tree instanceof NetworkCatalogRootTree) {
			if (item.URLByType.get(NetworkCatalogItem.URL_CATALOG) != null) {
				addMenuItem(menu, EXPAND_OR_COLLAPSE_TREE_ITEM_ID,
					isOpened ? "closeCatalog" : "openCatalog");
			}
			if (isOpened) {
				addMenuItem(menu, RELOAD_ITEM_ID, "reload");
			}
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
					if (mgr.registrationSupported()) {
						//registerAction(new RegisterUserAction(mgr), true);
					}
					if (mgr.passwordRecoverySupported()) {
						//registerAction(new PasswordRecoveryAction(mgr), true);
					}
				}
			}
			//addMenuItem(DONT_SHOW_ITEM_ID, "dontShow"); // TODO: is it needed??? and how to turn it on???
		} else {
			if (item.URLByType.get(NetworkCatalogItem.URL_CATALOG) != null) {
				addMenuItem(menu, EXPAND_OR_COLLAPSE_TREE_ITEM_ID,
					isOpened ? "collapseTree" : "expandTree");
			}
			if (item.URLByType.get(NetworkCatalogItem.URL_HTML_PAGE) != null) {
				addMenuItem(menu, OPEN_IN_BROWSER_ITEM_ID, "openInBrowser");
			}
			if (isOpened) {
				addMenuItem(menu, RELOAD_ITEM_ID, "reload");
			}
		}
		/*if (tree.hasChildren()) {
			menu.add(0, DBG_UNLOAD_CATALOG_ITEM_ID, 0, "Unload catalog");
		}*/
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
	public boolean runAction(NetworkTree tree, int actionCode) {
		switch (actionCode) {
			case EXPAND_OR_COLLAPSE_TREE_ITEM_ID:
				doExpandCatalog((NetworkCatalogTree)tree);
				return true;
			case OPEN_IN_BROWSER_ITEM_ID:
				if (NetworkLibraryActivity.Instance != null) {
					NetworkLibraryActivity.Instance.openInBrowser(((NetworkCatalogTree)tree).Item.URLByType.get(NetworkCatalogItem.URL_HTML_PAGE));
				}
				return true;
			case RELOAD_ITEM_ID:
				doReloadCatalog((NetworkCatalogTree)tree);
				return true;
			/*case DONT_SHOW_ITEM_ID:
				diableCatalog((NetworkCatalogRootTree) tree);
				return true;*/
			case SIGNIN_ITEM_ID:
				AuthenticationDialog.Instance().show(((NetworkCatalogTree)tree).Item.Link, null);
				return true;
			case SIGNOUT_ITEM_ID:
				doSignOut((NetworkCatalogTree)tree);
				return true;
			case REFILL_ACCOUNT_ITEM_ID:
				if (NetworkLibraryActivity.Instance != null) {
					NetworkLibraryActivity.Instance.openInBrowser(((NetworkCatalogTree)tree).Item.Link.authenticationManager().refillAccountLink());
				}
				return true;
			/*case DBG_UNLOAD_CATALOG_ITEM_ID: {
					final NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
					final ZLTreeAdapter adapter = NetworkLibraryActivity.Instance.getAdapter();
					if (tree.hasChildren() && adapter.isOpen(tree)) {
						adapter.expandOrCollapseTree(tree);
					}
					catalogTree.ChildrenItems.clear();
					tree.clear();
					adapter.resetTree();
				}
				return true;*/
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

	private static final int WHAT_UPDATE_ITEMS = 0;
	private static final int WHAT_FINISHED = 1;

	private class ExpandCatalogHandler extends Handler {

		private final NetworkCatalogTree myTree;
		private final LinkedList<NetworkLibraryItem> myItems = new LinkedList<NetworkLibraryItem>();

		ExpandCatalogHandler(NetworkCatalogTree tree) {
			myTree = tree;
		}

		void addItem(NetworkLibraryItem item) {
			synchronized (myItems) {
				myItems.add(item);
			}
		}

		void ensureFinish() {
			synchronized (myItems) {
				while (myItems.size() > 0) {
					try {
						myItems.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}

		private void displayItems() {
			synchronized (myItems) {
				for (NetworkLibraryItem item: myItems) {
					myTree.ChildrenItems.add(item);
					NetworkTreeFactory.createNetworkTree(myTree, item);
				}
				myItems.clear();
				myItems.notifyAll(); // wake up process, that waits for all items to be displayed (see ensureFinish() method)
			}
			if (NetworkLibraryActivity.Instance != null) {
				NetworkLibraryActivity.Instance.resetTree();
			}
		}

		private void onUpdateItems() {
			boolean expand = !myTree.hasChildren();
			displayItems();
			if (expand && NetworkLibraryActivity.Instance != null) {
				ZLTreeAdapter adapter = NetworkLibraryActivity.Instance.getAdapter();
				if (adapter != null) {
					adapter.expandOrCollapseTree(myTree);
				}
			}
		}

		private void onFinish(String err) {
			afterUpdateCatalog(err, myTree.ChildrenItems.size() == 0);
			endProcessingTree(myTree);
		}

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case WHAT_UPDATE_ITEMS:
				onUpdateItems();
				break;
			case WHAT_FINISHED:
				onFinish((String) message.obj);
				break;
			}
		}
	}

	private static class ExpandCatalogRunnable implements Runnable {

		private final NetworkCatalogTree myTree;
		private final boolean myCheckAuthentication;
		private final ExpandCatalogHandler myHandler;

		public ExpandCatalogRunnable(NetworkCatalogTree tree, ExpandCatalogHandler handler, boolean checkAuthentication) {
			myTree = tree;
			myHandler = handler;
			myCheckAuthentication = checkAuthentication;
		}

		public void run() {
			/*if (!NetworkOperationRunnable::tryConnect()) {
				return;
			}*/
			final NetworkLink link = myTree.Item.Link;
			if (myCheckAuthentication && link.authenticationManager() != null) {
				NetworkAuthenticationManager mgr = link.authenticationManager();
				AuthenticationStatus auth = mgr.isAuthorised(true);
				if (auth.Message != null) {
					myHandler.sendMessage(myHandler.obtainMessage(WHAT_FINISHED, auth.Message));
					return;
				}
				if (auth.Status == ZLBoolean3.B3_TRUE && mgr.needsInitialization()) {
					final String err = mgr.initialize();
					if (err != null) {
						mgr.logOut();
					}
				}
			}
			final String err = myTree.Item.loadChildren(new NetworkCatalogItem.CatalogListener() {
				private long myUpdateTime;
				public void onNewItem(NetworkLibraryItem item) {
					myHandler.addItem(item);
					final long now = System.currentTimeMillis();
					if (now > myUpdateTime) {
						myHandler.sendEmptyMessage(WHAT_UPDATE_ITEMS);
						myUpdateTime = now + 1000; // update interval == 1000 milliseconds; FIXME: hardcoded const
					}
				}
			});
			myHandler.sendEmptyMessage(WHAT_UPDATE_ITEMS);
			myHandler.ensureFinish();
			myHandler.sendMessage(myHandler.obtainMessage(WHAT_FINISHED, err));
		}
	}

	public void doExpandCatalog(final NetworkCatalogTree tree) {
		if (NetworkLibraryActivity.Instance == null) {
			return;
		}
		if (tree.hasChildren()) {
			NetworkLibraryActivity.Instance.getAdapter().expandOrCollapseTree(tree);
			return;
		}
		if (!startProcessingTree(tree)) {
			return;
		}
		final String url = tree.Item.URLByType.get(NetworkCatalogItem.URL_CATALOG);
		if (url == null) {
			throw new RuntimeException("That's impossible!!!");
		}
		final ExpandCatalogHandler handler = new ExpandCatalogHandler(tree);
		NetworkLibraryActivity.Instance.loadCatalog(
			Uri.parse(url),
			new ExpandCatalogRunnable(tree, handler, true)
		);
	}

	public void doReloadCatalog(final NetworkCatalogTree tree) {
		if (NetworkLibraryActivity.Instance == null) {
			return;
		}
		if (!startProcessingTree(tree)) {
			return;
		}
		final String url = tree.Item.URLByType.get(NetworkCatalogItem.URL_CATALOG);
		if (url == null) {
			throw new RuntimeException("That's impossible!!!");
		}
		NetworkLibraryActivity.Instance.getAdapter().expandOrCollapseTree(tree);
		tree.ChildrenItems.clear();
		tree.clear();
		NetworkLibraryActivity.Instance.getAdapter().resetTree();
		final ExpandCatalogHandler handler = new ExpandCatalogHandler(tree);
		NetworkLibraryActivity.Instance.loadCatalog(
			Uri.parse(url),
			new ExpandCatalogRunnable(tree, handler, false)
		);
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
			if (NetworkLibraryActivity.Instance != null) {
				final ZLResource buttonResource = dialogResource.getResource("button");
				new AlertDialog.Builder(NetworkLibraryActivity.Instance)
					.setTitle(boxResource.getResource("title").getValue())
					.setMessage(msg)
					.setIcon(0)
					.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
					.create().show();
			}
			// TODO: else show notification???
		}
		final NetworkLibrary library = NetworkLibrary.Instance();
		library.invalidateAccountDependents();
		library.synchronize();
		if (NetworkLibraryActivity.Instance != null) {
			NetworkLibraryActivity.Instance.resetTree();
		}
	}

	/*public void diableCatalog(NetworkCatalogRootTree tree) {
		tree.Link.OnOption.setValue(false);
		final NetworkLibrary library = NetworkLibrary.Instance();
		library.invalidate();
		library.synchronize();
		NetworkLibraryActivity.Instance.resetTree(); // FIXME: may be bug: [open catalog] -> [disable] -> [enable] -> [load againg] => catalog won't opens (it will be closed after previos opening)
	}*/

	private void doSignOut(NetworkCatalogTree tree) {
		if (NetworkLibraryActivity.Instance == null) {
			return;
		}
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				final NetworkLibrary library = NetworkLibrary.Instance();
				library.invalidateAccountDependents();
				library.synchronize();
				if (NetworkLibraryActivity.Instance != null) {
					NetworkLibraryActivity.Instance.resetTree();
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
		((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("signOut", runnable, NetworkLibraryActivity.Instance);
	}
}
