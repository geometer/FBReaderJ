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

import android.app.*;
import android.os.Message;
import android.os.Handler;
import android.view.ContextMenu;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.content.Intent;
import android.content.Context;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

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

	public static final int DBG_UNLOAD_CATALOG_ITEM_ID = 128;

	private ZLTreeAdapter myAdapter;

	private int dbgCatalogsCounter;
	private int dbgBooksCounter;

	public NetworkCatalogActions(NetworkLibraryActivity activity, ZLTreeAdapter adapter) {
		super(activity);
		myAdapter = adapter;
	}

	@Override
	public boolean canHandleTree(NetworkTree tree) {
		return tree instanceof NetworkCatalogTree;
	}

	@Override
	public void buildContextMenu(ContextMenu menu, NetworkTree tree) {
		final NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
		final NetworkCatalogItem item = catalogTree.Item;
		menu.setHeaderTitle(tree.getName());
		final boolean isOpened = tree.hasChildren() && myAdapter.isOpen(tree);
		if (tree instanceof NetworkCatalogRootTree) {
			if (item.LinkByType.get(NetworkLibraryItem.URL_CATALOG) != null) {
				addMenuItem(menu, EXPAND_OR_COLLAPSE_TREE_ITEM_ID,
					isOpened ? "closeCatalog" : "openCatalog");
			}
			if (isOpened) {
				addMenuItem(menu, RELOAD_ITEM_ID, "reload");
			}
			//NetworkAuthenticationManager mgr = item.Link.authenticationManager();
			/*if (!mgr.isNull()) {
				registerAction(new LoginAction(*mgr));
				registerAction(new LogoutAction(*mgr));
				if (!mgr->refillAccountLink().empty()) {
					registerAction(new RefillAccountAction(*mgr));
				}
				if (mgr->registrationSupported()) {
					registerAction(new RegisterUserAction(*mgr), true);
				}
				if (mgr->passwordRecoverySupported()) {
					registerAction(new PasswordRecoveryAction(*mgr), true);
				}
			}*/
			//addMenuItem(DONT_SHOW_ITEM_ID, "dontShow"); // TODO: is it needed??? and how to turn it on???
		} else {
			if (item.LinkByType.get(NetworkLibraryItem.URL_CATALOG) != null) {
				addMenuItem(menu, EXPAND_OR_COLLAPSE_TREE_ITEM_ID,
					isOpened ? "collapseTree" : "expandTree");
			}
			if (item.LinkByType.get(NetworkLibraryItem.URL_HTML_PAGE) != null) {
				addMenuItem(menu, OPEN_IN_BROWSER_ITEM_ID, "openInBrowser");
			}
			if (isOpened) {
				addMenuItem(menu, RELOAD_ITEM_ID, "reload");
			}
		}
		if (tree.hasChildren()) {
			menu.add(0, DBG_UNLOAD_CATALOG_ITEM_ID, 0, "Unload catalog");
		}
	}

	@Override
	public int getDefaultActionCode(NetworkTree tree) {
		final NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
		final NetworkCatalogItem item = catalogTree.Item;
		if (item.LinkByType.get(NetworkLibraryItem.URL_CATALOG) != null) {
			return EXPAND_OR_COLLAPSE_TREE_ITEM_ID;
		}
		if (item.LinkByType.get(NetworkLibraryItem.URL_HTML_PAGE) != null) {
			return OPEN_IN_BROWSER_ITEM_ID;
		}
		return -1;
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
				myActivity.openInBrowser(((NetworkCatalogTree)tree).Item.LinkByType.get(NetworkLibraryItem.URL_HTML_PAGE).URL);
				return true;
			case RELOAD_ITEM_ID:
				doReloadCatalog((NetworkCatalogTree)tree);
				return true;
			/*case DONT_SHOW_ITEM_ID:
				diableCatalog((NetworkCatalogRootTree) tree);
				return true;*/
			case DBG_UNLOAD_CATALOG_ITEM_ID: {
					final NetworkCatalogTree catalogTree = (NetworkCatalogTree) tree;
					if (tree.hasChildren() && myAdapter.isOpen(tree)) {
						myAdapter.expandOrCollapseTree(tree);
					}
					catalogTree.ChildrenItems.clear();
					tree.clear();
					myAdapter.resetTree();
				}
				return true;
		}
		return false;
	}


	private static final LinkedList<NetworkTree> ourProcessingTrees = new LinkedList<NetworkTree>();
	private static final int ourProcessingNotificationId = (int) System.currentTimeMillis();

	private void updateProgressNotification(NetworkCatalogTree tree) {
		final RemoteViews contentView = new RemoteViews(myActivity.getPackageName(), R.layout.download_notification);
		String title = getTitleValue("downloadingCatalogs");
		contentView.setTextViewText(R.id.download_notification_title, title);
		contentView.setTextViewText(R.id.download_notification_progress_text, "");
		contentView.setProgressBar(R.id.download_notification_progress_bar, 100, 0, true);

		//final Intent intent = new Intent(myActivity, NetworkLibraryActivity.class);
		//final PendingIntent contentIntent = PendingIntent.getActivity(myActivity, 0, intent, 0);
		final PendingIntent contentIntent = PendingIntent.getActivity(myActivity, 0, new Intent(), 0);

		final Notification notification = new Notification();
		notification.icon = android.R.drawable.stat_notify_sync;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.contentView = contentView;
		notification.contentIntent = contentIntent;
		notification.number = ourProcessingTrees.size();

		final NotificationManager notificationManager = (NotificationManager) myActivity.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(ourProcessingNotificationId, notification);
	}

	private boolean startProgressNotification(NetworkCatalogTree tree) {
		if (ourProcessingTrees.contains(tree)) {
			return false;
		}
		ourProcessingTrees.add(tree);
		updateProgressNotification(tree);
		return true;
	}

	private void endProgressNotification(NetworkCatalogTree tree) {
		ourProcessingTrees.remove(tree);
		if (ourProcessingTrees.size() == 0) {
			final NotificationManager notificationManager = (NotificationManager) myActivity.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(ourProcessingNotificationId);
		} else {
			updateProgressNotification(tree);
		}
	}

	public void doExpandCatalog(final NetworkCatalogTree tree) {
		if (tree.hasChildren()) {
			myAdapter.expandOrCollapseTree(tree);
			return;
		}
		if (!startProgressNotification(tree)) {
			return;
		}
		//final boolean hadChildren = false;
		final Handler progressHandler = new Handler() {
			public void handleMessage(Message message) {
				NetworkLibraryItem item = (NetworkLibraryItem) message.obj;
				boolean expand = !tree.hasChildren();
				tree.ChildrenItems.add(item);
				NetworkTreeFactory.createNetworkTree(tree, item);
				tree.updateAccountDependents();
				
				if (item instanceof NetworkCatalogItem) {
					++dbgCatalogsCounter;
				} if (item instanceof NetworkBookItem) {
					++dbgBooksCounter;
				}
				myAdapter.resetTree();
				if (expand) {
					// If catalog loading started => Tree must be expanded after the first item has been loaded
					myAdapter.expandOrCollapseTree(tree);
				}
			}
		};
		final Handler finishHandler = new Handler() {
			public void handleMessage(Message message) {
				afterUpdateCatalog((String) message.obj, tree.ChildrenItems.size() == 0);
				endProgressNotification(tree);
				System.err.println("FBREADER -- dbgCatalogsCounter = " + dbgCatalogsCounter);
				System.err.println("FBREADER -- dbgBooksCounter = " + dbgBooksCounter);
			}
		};
		new Thread(new Runnable() {
			public void run() {
				/*if (!NetworkOperationRunnable::tryConnect()) {
					return;
				}*/
				NetworkCatalogItem item = tree.Item;
				NetworkLink link = item.Link;
				if (link.authenticationManager() != null) {
					NetworkAuthenticationManager mgr = link.authenticationManager();
					/*IsAuthorisedRunnable checker(mgr);
					checker.executeWithUI();
					if (checker.hasErrors()) {
						checker.showErrorMessage();
						return;
					}
					if (checker.result() == B3_TRUE && mgr.needsInitialization()) {
						InitializeAuthenticationManagerRunnable initializer(mgr);
						initializer.executeWithUI();
						if (initializer.hasErrors()) {
							LogOutRunnable logout(mgr);
							logout.executeWithUI();
						}
					}*/
				}
				final String err = tree.Item.loadChildren(new NetworkCatalogItem.CatalogListener() {
					public void onNewItem(NetworkLibraryItem item) {
						progressHandler.sendMessage(progressHandler.obtainMessage(0, item));
					}
				});
				finishHandler.sendMessage(finishHandler.obtainMessage(0, err));
			}
		}).start();
	}

	public void doReloadCatalog(final NetworkCatalogTree tree) {
		if (!startProgressNotification(tree)) {
			return;
		}
		final Handler progressHandler = new Handler() {
			public void handleMessage(Message message) {
				NetworkLibraryItem item = (NetworkLibraryItem) message.obj;
				boolean expand = !tree.hasChildren();
				tree.ChildrenItems.add(item);
				NetworkTreeFactory.createNetworkTree(tree, item);
				tree.updateAccountDependents();
				myAdapter.resetTree();
				if (expand) {
					// If catalog loading started => Tree must be expanded after the first item has been loaded
					myAdapter.expandOrCollapseTree(tree);
				}
			}
		};
		final Handler finishHandler = new Handler() {
			public void handleMessage(Message message) {
				afterUpdateCatalog((String) message.obj, tree.ChildrenItems.size() == 0);
				endProgressNotification(tree);
			}
		};
		myAdapter.expandOrCollapseTree(tree);
		tree.ChildrenItems.clear();
		tree.clear();
		myAdapter.resetTree();
		new Thread(new Runnable() {
			public void run() {
				final String err = tree.Item.loadChildren(new NetworkCatalogItem.CatalogListener() {
					public void onNewItem(NetworkLibraryItem item) {
						progressHandler.sendMessage(progressHandler.obtainMessage(0, item));
					}
				});
				finishHandler.sendMessage(finishHandler.obtainMessage(0, err));
			}
		}).start();
	}

	private void afterUpdateCatalog(String errorMessage, boolean childrenEmpty) {
		if (errorMessage != null) {
			final ZLResource dialogResource = ZLResource.resource("dialog");
			final ZLResource buttonResource = dialogResource.getResource("button");
			final ZLResource boxResource = dialogResource.getResource("networkError");
			new AlertDialog.Builder(myActivity)
				.setTitle(boxResource.getResource("title").getValue())
				.setMessage(errorMessage)
				.setIcon(0)
				.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
				.create().show();
		} else if (childrenEmpty) {
			final ZLResource dialogResource = ZLResource.resource("dialog");
			final ZLResource buttonResource = dialogResource.getResource("button");
			final ZLResource boxResource = dialogResource.getResource("emptyCatalogBox");
			new AlertDialog.Builder(myActivity)
				.setTitle(boxResource.getResource("title").getValue())
				.setMessage(boxResource.getResource("message").getValue())
				.setIcon(0)
				.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
				.create().show();
		}
		final NetworkLibrary library = NetworkLibrary.Instance();
		library.invalidateAccountDependents();
		library.synchronize();
		myAdapter.resetTree();
	}

	/*public void diableCatalog(NetworkCatalogRootTree tree) {
		tree.Link.OnOption.setValue(false);
		final NetworkLibrary library = NetworkLibrary.Instance();
		library.invalidate();
		library.synchronize();
		myAdapter.resetTree(); // FIXME: may be bug: [open catalog] -> [disable] -> [enable] -> [load againg] => catalog won't opens (it will be closed after previos opening)
	}*/

}
