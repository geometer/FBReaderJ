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
import android.content.Intent;
import android.view.Menu;
import android.view.ContextMenu;

import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.android.util.PackageUtil;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.*;
import org.geometerplus.fbreader.network.tree.NetworkTreeFactory;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.tree.NetworkCatalogRootTree;
import org.geometerplus.fbreader.network.opds.BasketItem;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

class NetworkCatalogActions extends NetworkTreeActions {
	public static final int OPEN_CATALOG_ITEM_ID = 0;
	public static final int OPEN_IN_BROWSER_ITEM_ID = 1;
	public static final int RELOAD_ITEM_ID = 2;
	public static final int SIGNUP_ITEM_ID = 3;
	public static final int SIGNIN_ITEM_ID = 4;
	public static final int SIGNOUT_ITEM_ID = 5;
	public static final int TOPUP_ITEM_ID = 6;

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
			urlItem != null ? urlItem.getUrl(UrlInfo.Type.Catalog) : null;
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
						if (Util.isTopupSupported(activity, item.Link)) {
							final String account = mgr.currentAccount();
							if (account != null) {
								addMenuItem(menu, TOPUP_ITEM_ID, "topup", account);
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
			if (urlItem != null && urlItem.getUrl(UrlInfo.Type.HtmlPage) != null) {
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
		if (urlItem.getUrl(UrlInfo.Type.Catalog) != null) {
			return OPEN_CATALOG_ITEM_ID;
		}
		if (urlItem.getUrl(UrlInfo.Type.HtmlPage) != null) {
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
		addOptionsItem(menu, TOPUP_ITEM_ID, "topup");
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
			urlItem.getUrl(UrlInfo.Type.Catalog) != null &&
			ItemsLoadingService.getRunnable(tree) == null
		);

		boolean signIn = false;
		boolean signOut = false;
		boolean topup = false;
		String userName = null;
		String account = null;
		NetworkAuthenticationManager mgr = item.Link.authenticationManager();
		if (mgr != null) {
			if (mgr.mayBeAuthorised(false)) {
				userName = mgr.currentUserName();
				signOut = true;
				account = mgr.currentAccount();
				if (account != null && Util.isTopupSupported(activity, item.Link)) {
					topup = true;
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
		prepareOptionsItem(menu, TOPUP_ITEM_ID, topup);
		return true;
	}

	private boolean consumeByVisibility(final NetworkBaseActivity activity, final NetworkTree tree, final int actionCode) {
		final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
		switch (item.getVisibility()) {
			case B3_TRUE:
				return false;
			case B3_UNDEFINED:
				Util.runAuthenticationDialog(activity, item.Link, null, new Runnable() {
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
		final NetworkCatalogTree catalogTree = (NetworkCatalogTree)tree;
		if (consumeByVisibility(activity, catalogTree, actionCode)) {
			return true;
		}

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
						item.getUrl(UrlInfo.Type.HtmlPage)
					);
				}
				return true;
			case RELOAD_ITEM_ID:
				doReloadCatalog(activity, catalogTree);
				return true;
			case SIGNIN_ITEM_ID:
				Util.runAuthenticationDialog(activity, item.Link, null, null);
				return true;
			case SIGNUP_ITEM_ID:
				Util.runRegistrationDialog(activity, item.Link);
				return true;
			case SIGNOUT_ITEM_ID:
				doSignOut(activity, catalogTree);
				return true;
			case TOPUP_ITEM_ID:
				new TopupActions().runStandalone(activity, item.Link);
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

	private static class CatalogExpander extends ItemsLoader {
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
			final NetworkCatalogActivity activity = NetworkCatalogActivity.getByTree(myTree);
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


	private static void processExtraData(final Activity activity, Map<String,String> extraData, final Runnable postRunnable) {
		if (extraData != null && !extraData.isEmpty()) {
			PackageUtil.runInstallPluginDialog(activity, extraData, postRunnable);
		} else {
			postRunnable.run();
		}
	}

	static void doExpandCatalog(final Activity activity, final NetworkCatalogTree tree) {
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
						clearTree(activity, tree);
					}
				}

				/* FIXME: if catalog's loading will be very fast
				 * then it is possible that loading message is lost
				 * (see afterUpdateCatalog method).
				 * 
				 * For example, this can be fixed via adding method
				 * NetworkView.postCatalogLoadingResult, that will do the following:
				 * 1) If there is activity, then show message
				 * 2) If there is no activity, then save message, and show when activity is created
				 * 3) Remove unused messages (say, by timeout)
				 */
				ItemsLoadingService.start(
					activity,
					tree,
					new CatalogExpander(activity, tree, true, resumeNotLoad)
				);
				processExtraData(activity, tree.Item.extraData(), new Runnable() {
					public void run() {
						Util.openTree(activity, tree);
					}
				});
			}
		});
	}

	private static void clearTree(Activity activity, final NetworkCatalogTree tree) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				tree.ChildrenItems.clear();
				tree.clear();
				NetworkView.Instance().fireModelChanged();
			}
		});
	}

	public void doReloadCatalog(NetworkBaseActivity activity, final NetworkCatalogTree tree) {
		if (ItemsLoadingService.getRunnable(tree) != null) {
			return;
		}
		clearTree(activity, tree);
		ItemsLoadingService.start(
			activity,
			tree,
			new CatalogExpander(activity, tree, false, false)
		);
	}

	private void doSignOut(final NetworkBaseActivity activity, NetworkCatalogTree tree) {
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

	private void removeCustomLink(ICustomNetworkLink link) {
		final NetworkLibrary library = NetworkLibrary.Instance();
		library.removeCustomLink(link);
		library.synchronize();
		NetworkView.Instance().fireModelChangedAsync();
	}
}
