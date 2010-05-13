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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.Menu;

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

	public void initialize() {
		NetworkLibrary.Instance().synchronize();

		myActions.add(new NetworkBookActions());
		myActions.add(new NetworkCatalogActions());
		myActions.add(new SearchItemActions());
		myActions.add(new RefillAccountActions());
		myActions.trimToSize();

		myInitialized = true;
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

	public boolean prepareOptionsMenu(Menu menu, NetworkTree tree) {
		if (!isInitialized()) {
			return false;
		}
		final NetworkTreeActions actions = getActions(tree);
		if (actions != null) {
			return actions.prepareOptionsMenu(menu, tree);
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

	ItemsLoadingRunnable removeItemsLoadingRunnable(String key) {
		synchronized (myItemsLoadingRunnables) {
			return myItemsLoadingRunnables.remove(key);
		}
	}

	public final boolean containsItemsLoadingRunnable(String key) {
		return getItemsLoadingRunnable(key) != null;
	}

	public void tryResumeLoading(NetworkBaseActivity activity, NetworkTree tree, String key, Runnable expandRunnable) {
		final ItemsLoadingRunnable runnable = NetworkView.Instance().getItemsLoadingRunnable(key);
		if (runnable != null && runnable.tryResume()) {
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
	 * Loading covers
	 */

	private static class MinPriorityThreadFactory implements ThreadFactory {

		private final ThreadFactory myDefaultThreadFactory = Executors.defaultThreadFactory();

		public Thread newThread(Runnable r) {
			final Thread th = myDefaultThreadFactory.newThread(r);
			th.setPriority(Thread.MIN_PRIORITY);
			return th;
		}
	}

	private static final int COVER_LOADING_THREADS_NUMBER = 3; // TODO: how many threads ???

	private final ExecutorService myPool = Executors.newFixedThreadPool(COVER_LOADING_THREADS_NUMBER, new MinPriorityThreadFactory());

	private final HashMap<String, LinkedList<Runnable>> myOnCoverSyncRunnables = new HashMap<String, LinkedList<Runnable>>();

	private class CoverSynchronizedHandler extends Handler {
		@Override
		public void handleMessage(Message message) {
			final String imageUrl = (String) message.obj;
			final LinkedList<Runnable> runables = myOnCoverSyncRunnables.remove(imageUrl);
			for (Runnable runnable: runables) {
				runnable.run();
			}
		}

		public void fireMessage(String imageUrl) {
			sendMessage(obtainMessage(0, imageUrl));
		}
	};

	private final CoverSynchronizedHandler myCoverSynchronizedHandler = new CoverSynchronizedHandler();

	public void performCoverSynchronization(final NetworkImage image, Runnable finishRunnable) {
		if (myOnCoverSyncRunnables.containsKey(image.Url)) {
			return;
		}
		final LinkedList<Runnable> runnables = new LinkedList<Runnable>();
		if (finishRunnable != null) {
			runnables.add(finishRunnable);
		}
		myOnCoverSyncRunnables.put(image.Url, runnables);
		myPool.execute(new Runnable() {
			public void run() {
				image.synchronize();
				myCoverSynchronizedHandler.fireMessage(image.Url);
			}
		});
	}

	public final boolean isCoverLoading(String coverUrl) {
		return myOnCoverSyncRunnables.containsKey(coverUrl);
	}

	public void addCoverSynchronizationRunnable(String coverUrl, Runnable finishRunnable) {
		final LinkedList<Runnable> runnables = myOnCoverSyncRunnables.get(coverUrl);
		if (runnables != null && finishRunnable != null) {
			runnables.add(finishRunnable);
		}
	}


	/*
	 * Open Network URL in browser
	 */

	public void openInBrowser(Context context, String url) {
		if (url != null) {
			url = NetworkLibrary.Instance().rewriteUrl(url, true);
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		}
	}


	/*
	 * Notifying view's components from services
	 */

	public interface EventListener {
		void onModelChanged();
	}

	private LinkedList<EventListener> myEventListeners = new LinkedList<EventListener>();

	public final void addEventListener(EventListener listener) {
		if (listener != null) {
			myEventListeners.add(listener);
		}
	}

	public final void removeEventListener(EventListener listener) {
		myEventListeners.remove(listener);
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

	public SearchItemTree getSearchItemTree() {
		return mySearchItem;
	}
}
