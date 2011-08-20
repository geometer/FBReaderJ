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
import android.content.DialogInterface;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.view.Menu;
import android.view.ContextMenu;
import android.view.MenuItem;

import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.resources.ZLResource;

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
	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof NetworkCatalogTree;
	}

	@Override
	public void buildContextMenu(NetworkLibraryActivity activity, ContextMenu menu, NetworkTree tree) {
		final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
		final NetworkURLCatalogItem urlItem =
			item instanceof NetworkURLCatalogItem ? (NetworkURLCatalogItem)item : null;
		menu.setHeaderTitle(tree.getName());

		boolean hasItems = false;

		final String catalogUrl =
			urlItem != null ? urlItem.getUrl(UrlInfo.Type.Catalog) : null;
		if (catalogUrl != null &&
			(!(item instanceof BasketItem) || item.Link.basket().bookIds().size() > 0)) {
			addMenuItem(menu, ActionCode.OPEN_CATALOG, "openCatalog");
			hasItems = true;
		}

		if (tree instanceof NetworkCatalogRootTree) {
			if (item.getVisibility() == ZLBoolean3.B3_TRUE) {
				final NetworkAuthenticationManager mgr = item.Link.authenticationManager();
				if (mgr != null) {
					if (mgr.mayBeAuthorised(false)) {
						addMenuItem(menu, ActionCode.SIGNOUT, "signOut", mgr.currentUserName());
						if (TopupMenuActivity.isTopupSupported(item.Link)) {
							final String account = mgr.currentAccount();
							if (account != null) {
								addMenuItem(menu, ActionCode.TOPUP, "topup", account);
							}
						}
					} else {
						addMenuItem(menu, ActionCode.SIGNIN, "signIn");
						//if (mgr.passwordRecoverySupported()) {
						//	registerAction(new PasswordRecoveryAction(mgr), true);
						//}
					}
				}
			}
			INetworkLink link = item.Link; 
			if (link instanceof ICustomNetworkLink) {
				addMenuItem(menu, ActionCode.CUSTOM_CATALOG_EDIT, "editCustomCatalog");
				addMenuItem(menu, ActionCode.CUSTOM_CATALOG_REMOVE, "removeCustomCatalog");
			}
		} else {
			if (urlItem != null && urlItem.getUrl(UrlInfo.Type.HtmlPage) != null) {
				addMenuItem(menu, ActionCode.OPEN_IN_BROWSER, "openInBrowser");
				hasItems = true;
			}
		}

		if (item.getVisibility() == ZLBoolean3.B3_UNDEFINED &&
			!hasItems && item.Link.authenticationManager() != null) {
			addMenuItem(menu, ActionCode.SIGNIN, "signIn");
		}
	}

	@Override
	public int getDefaultActionCode(NetworkLibraryActivity activity, NetworkTree tree) {
		final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
		if (!(item instanceof NetworkURLCatalogItem)) {
			return ActionCode.OPEN_CATALOG;
		}
		final NetworkURLCatalogItem urlItem = (NetworkURLCatalogItem)item;
		if (urlItem.getUrl(UrlInfo.Type.Catalog) != null) {
			return ActionCode.OPEN_CATALOG;
		}
		if (urlItem.getUrl(UrlInfo.Type.HtmlPage) != null) {
			return ActionCode.OPEN_IN_BROWSER;
		}
		if (urlItem.getVisibility() == ZLBoolean3.B3_UNDEFINED &&
			urlItem.Link.authenticationManager() != null) {
			return ActionCode.SIGNIN;
		}
		return ActionCode.TREE_NO_ACTION;
	}

	private boolean consumeByVisibility(final NetworkLibraryActivity activity, final NetworkTree tree, final int actionCode) {
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
						if (actionCode != ActionCode.SIGNIN) {
							runAction(activity, tree, actionCode);
						}
					}
				});
				break;
		}
		return true;
	}

	@Override
	public boolean runAction(final NetworkLibraryActivity activity, NetworkTree tree, int actionCode) {
		final NetworkCatalogTree catalogTree = (NetworkCatalogTree)tree;
		if (consumeByVisibility(activity, catalogTree, actionCode)) {
			return true;
		}

		final NetworkCatalogItem item = catalogTree.Item;
		switch (actionCode) {
			case ActionCode.OPEN_CATALOG:
				if (item instanceof BasketItem && item.Link.basket().bookIds().size() == 0) {
					UIUtil.showErrorMessage(activity, "emptyBasket");
				} else {
					doExpandCatalog(activity, catalogTree);
				}
				return true;
			case ActionCode.OPEN_IN_BROWSER:
				if (item instanceof NetworkURLCatalogItem) {
					final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
					final String message = NetworkLibrary.resource().getResource("confirmQuestions").getResource("openInBrowser").getValue();
					new AlertDialog.Builder(activity)
						.setTitle(catalogTree.getName())
						.setMessage(message)
						.setIcon(0)
						.setPositiveButton(buttonResource.getResource("yes").getValue(), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Util.openInBrowser(activity, item.getUrl(UrlInfo.Type.HtmlPage));
							}
						})
						.setNegativeButton(buttonResource.getResource("no").getValue(), null)
						.create().show();
				}
				return true;
			case ActionCode.RELOAD_CATALOG:
				doReloadCatalog(activity, catalogTree);
				return true;
			case ActionCode.SIGNIN:
				Util.runAuthenticationDialog(activity, item.Link, null, null);
				return true;
			case ActionCode.SIGNUP:
				Util.runRegistrationDialog(activity, item.Link);
				return true;
			case ActionCode.SIGNOUT:
				doSignOut(activity, catalogTree);
				return true;
			case ActionCode.TOPUP:
				// TODO: replace 112 with required amount
				TopupMenuActivity.runMenu(activity, item.Link, "112");
				return true;
			case ActionCode.CUSTOM_CATALOG_EDIT:
			{
				final Intent intent = new Intent(activity, AddCustomCatalogActivity.class);
				AddCustomCatalogActivity.addLinkToIntent(
					intent,
					(ICustomNetworkLink)item.Link
				);
				activity.startActivity(intent);
				return true;
			}
			case ActionCode.CUSTOM_CATALOG_REMOVE:
				removeCustomLink((ICustomNetworkLink)item.Link);
				return true;
			case ActionCode.BASKET_CLEAR:
				item.Link.basket().clear();
				return true;
			case ActionCode.BASKET_BUY_ALL_BOOKS:
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

	public void doReloadCatalog(NetworkLibraryActivity activity, final NetworkCatalogTree tree) {
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

	private void doSignOut(final NetworkLibraryActivity activity, NetworkCatalogTree tree) {
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
