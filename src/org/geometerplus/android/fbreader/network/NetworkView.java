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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.Menu;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;

class NetworkView {
	private static NetworkView ourInstance;

	public static NetworkView Instance() {
		if (ourInstance == null) {
			ourInstance = new NetworkView();
		}
		return ourInstance;
	}

	private NetworkView() {
	}


	private volatile boolean myInitialized;

	public boolean isInitialized() {
		return myInitialized;
	}

	public void initialize() throws ZLNetworkException {
		new SQLiteNetworkDatabase();

		final NetworkLibrary library = NetworkLibrary.Instance();
		library.initialize();
		library.synchronize();

		myActions.add(new NetworkBookActions());
		myActions.add(new NetworkCatalogActions());
		myActions.add(new SearchItemActions());
		myActions.add(new RefillAccountActions());
		myActions.add(new AddCustomCatalogItemActions());
		myActions.trimToSize();

		myInitialized = true;
	}

	public void runBackgroundUpdate(boolean clearCache) throws ZLNetworkException {
		NetworkLibrary.Instance().runBackgroundUpdate(clearCache);
	}

	// This method MUST be called from main thread
	// This method has effect only when runBackgroundUpdate method has returned null
	public void finishBackgroundUpdate() {
		NetworkLibrary library = NetworkLibrary.Instance();
		library.finishBackgroundUpdate();
		library.synchronize();
		fireModelChanged();
	}

	/*
	 * NetworkLibraryItem's actions
	 */

	private final ArrayList<NetworkTreeActions> myActions = new ArrayList<NetworkTreeActions>();

	public NetworkTreeActions getActions(NetworkTree tree) {
		for (NetworkTreeActions actions: myActions) {
			if (actions.canHandleTree(tree)) {
				return actions;
			}
		}
		return null;
	}

	/*
	 * OptionsMenu methods
	 */

	public boolean createOptionsMenu(Menu menu, NetworkTree tree) {
		if (!isInitialized()) {
			return false;
		}
		final NetworkTreeActions actions = getActions(tree);
		if (actions != null) {
			return actions.createOptionsMenu(menu, tree);
		}
		return false;
	}

	public boolean prepareOptionsMenu(NetworkBaseActivity activity, Menu menu, NetworkTree tree) {
		if (!isInitialized()) {
			return false;
		}
		final NetworkTreeActions actions = getActions(tree);
		if (actions != null) {
			return actions.prepareOptionsMenu(activity, menu, tree);
		}
		return false;
	}

	public boolean runOptionsMenu(NetworkBaseActivity activity, MenuItem item, NetworkTree tree) {
		if (!isInitialized()) {
			return false;
		}
		final NetworkTreeActions actions = getActions(tree);
		if (actions != null) {
			return actions.runAction(activity, tree, item.getItemId());
		}
		return false;
	}

	/*
	 * Code for loading network items (running items-loading service and managing items-loading runnables).
	 */

	private final HashMap<String, ItemsLoadingRunnable> myItemsLoadingRunnables = new HashMap<String, ItemsLoadingRunnable>();

	public void startItemsLoading(Context context, String key, ItemsLoadingRunnable runnable) {
		boolean doDownload = false;
		synchronized (myItemsLoadingRunnables) {
			if (!myItemsLoadingRunnables.containsKey(key)) {
				myItemsLoadingRunnables.put(key, runnable);
				doDownload = true;
			}
		}
		if (doDownload) {
			context.startService(
				new Intent(context.getApplicationContext(), ItemsLoadingService.class)
					.putExtra(ItemsLoadingService.ITEMS_LOADING_RUNNABLE_KEY, key)
			);
		}
	}

	ItemsLoadingRunnable getItemsLoadingRunnable(String key) {
		synchronized (myItemsLoadingRunnables) {
			return myItemsLoadingRunnables.get(key);
		}
	}

	void removeItemsLoadingRunnable(String key) {
		synchronized (myItemsLoadingRunnables) {
			ItemsLoadingRunnable runnable = myItemsLoadingRunnables.remove(key);
			if (runnable != null) {
				runnable.runFinishHandler();
			}
		}
	}

	public final boolean containsItemsLoadingRunnable(String key) {
		return getItemsLoadingRunnable(key) != null;
	}

	public void tryResumeLoading(NetworkBaseActivity activity, NetworkTree tree, String key, Runnable expandRunnable) {
		final ItemsLoadingRunnable runnable = getItemsLoadingRunnable(key);
		if (runnable != null && runnable.tryResumeLoading()) {
			openTree(activity, tree, key);
			return;
		}
		if (runnable == null) {
			expandRunnable.run();
		} else {
			runnable.runOnFinish(expandRunnable);
		}
	}


	/*
	 * Notifying view's components from services
	 */

	public interface EventListener {
		void onModelChanged();
	}

	private Handler myEventHandler;
	private LinkedList<EventListener> myEventListeners = new LinkedList<EventListener>();

	/*
	 * This method must be called only from main thread
	 */
	public final void addEventListener(EventListener listener) {
		synchronized (myEventListeners) {
			if (myEventHandler == null) {
				myEventHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						fireModelChanged();
					}
				};
			}
			if (listener != null) {
				myEventListeners.add(listener);
			}
		}
	}

	public final void removeEventListener(EventListener listener) {
		synchronized (myEventListeners) {
			myEventListeners.remove(listener);
		}
	}

	final void fireModelChangedAsync() {
		synchronized (myEventListeners) {
			if (myEventHandler != null) {
				myEventHandler.sendEmptyMessage(0);
			}
		}
	}

	final void fireModelChanged() {
		for (EventListener listener: myEventListeners) {
			listener.onModelChanged();
		}
	}


	/*
	 * Starting BookInfo activity
	 */

	private NetworkBookItem myBookInfoItem;

	public void showBookInfoActivity(Context context, NetworkBookItem book) {
		myBookInfoItem = book;
		context.startActivity(
			new Intent(context.getApplicationContext(), NetworkBookInfoActivity.class)
		);
	}

	NetworkBookItem getBookInfoItem() {
		return myBookInfoItem;
	}


	/*
	 * Opening Catalogs & managing opened catalogs stack
	 */

	private final LinkedList<NetworkTree> myOpenedStack = new LinkedList<NetworkTree>();
	private final HashMap<String, NetworkCatalogActivity> myOpenedActivities = new HashMap<String, NetworkCatalogActivity>();

	public void openTree(Context context, NetworkTree tree, String key) {
		final int level = tree.Level - 1; // tree.Level == 1 for catalog's root element
		if (level > myOpenedStack.size()) {
			throw new RuntimeException("Unable to open catalog with Level greater than the number of opened catalogs.\n"
				+ "Catalog: " + tree.getName() + "\n"
				+ "Level: " + level + "\n"
				+ "Opened catalogs: " + myOpenedStack.size());
		}
		while (level < myOpenedStack.size()) {
			myOpenedStack.removeLast();
		}
		myOpenedStack.add(tree);

		context.startActivity(
			new Intent(context.getApplicationContext(), NetworkCatalogActivity.class)
				.putExtra(NetworkCatalogActivity.CATALOG_LEVEL_KEY, level)
				.putExtra(NetworkCatalogActivity.CATALOG_KEY_KEY, key)
		);
	}

	void setOpenedActivity(String key, NetworkCatalogActivity activity) {
		if (activity == null) {
			myOpenedActivities.remove(key);
		} else {
			myOpenedActivities.put(key, activity);
		}
	}

	public NetworkCatalogActivity getOpenedActivity(String key) {
		return myOpenedActivities.get(key);
	}

	public NetworkTree getOpenedTree(int level) {
		if (level < 0 || level >= myOpenedStack.size()) {
			return null;
		}
		return myOpenedStack.get(level);
	}

	/*
	 * Special view items item
	 */

	private final SearchItemTree mySearchItem = new SearchItemTree();
	private final AddCustomCatalogItemTree myAddCustomCatalogItem = new AddCustomCatalogItemTree();

	public SearchItemTree getSearchItemTree() {
		return mySearchItem;
	}

	public AddCustomCatalogItemTree getAddCustomCatalogItemTree() {
		return myAddCustomCatalogItem;
	}
}
