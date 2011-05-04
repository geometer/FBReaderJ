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

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkItem;

abstract class ItemsLoadingHandler {
	private static final int WHAT_UPDATE_ITEMS = 0;
	private static final int WHAT_FINISHED = 1;

	private final Activity myActivity;

	private final LinkedList<NetworkItem> myItems = new LinkedList<NetworkItem>();
	private final HashMap<INetworkLink, LinkedList<NetworkItem>> myUncommitedItems = new HashMap<INetworkLink, LinkedList<NetworkItem>>();
	private final Object myItemsMonitor = new Object();

	private volatile boolean myFinishProcessed;
	private final Object myFinishMonitor = new Object();

	ItemsLoadingHandler(Activity activity) {
		myActivity = activity;
	}

	public final void addItem(INetworkLink link, NetworkItem item) {
		synchronized (myItemsMonitor) {
			myItems.add(item);
			LinkedList<NetworkItem> uncommited = myUncommitedItems.get(link);
			if (uncommited == null) {
				uncommited = new LinkedList<NetworkItem>();
				myUncommitedItems.put(link, uncommited);
			}
			uncommited.add(item);
		}
	}

	public final void commitItems(INetworkLink link) {
		synchronized (myItemsMonitor) {
			LinkedList<NetworkItem> uncommited = myUncommitedItems.get(link);
			if (uncommited != null) {
				uncommited.clear();
			}
		}
	}

	public final void ensureItemsProcessed() {
		synchronized (myItemsMonitor) {
			while (myItems.size() > 0) {
				try {
					myItemsMonitor.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public final void doUpdateItems() {
		synchronized (myItemsMonitor) {
			updateItems(myItems);
			myItems.clear();
			myItemsMonitor.notifyAll(); // wake up process, that waits for finish condition (see ensureFinish() method)
		}
	}

	public final void ensureFinishProcessed() {
		synchronized (myFinishMonitor) {
			while (!myFinishProcessed) {
				try {
					myFinishMonitor.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private final void doProcessFinish(String errorMessage, boolean interrupted) {
		HashSet<NetworkItem> uncommitedItems = new HashSet<NetworkItem>();
		synchronized (myUncommitedItems) {
			for (LinkedList<NetworkItem> items: myUncommitedItems.values()) {
				uncommitedItems.addAll(items);
			}
		}
		synchronized (myFinishMonitor) {
			onFinish(errorMessage, interrupted, uncommitedItems);
			myFinishProcessed = true;
			myFinishMonitor.notifyAll(); // wake up process, that waits for finish condition (see ensureFinish() method)
		}
	}

	public final void sendFinish(final String errorMessage, final boolean interrupted) {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				doProcessFinish(errorMessage, interrupted);
			}
		});
	}

	protected abstract void updateItems(List<NetworkItem> items);
	protected abstract void onFinish(String errorMessage, boolean interrupted, Set<NetworkItem> uncommitedItems);
}
