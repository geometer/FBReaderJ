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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.os.Message;
import android.os.Handler;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkLibraryItem;


abstract class ItemsLoadingHandler extends Handler {

	private static final int WHAT_UPDATE_ITEMS = 0;
	private static final int WHAT_FINISHED = 1;

	private final LinkedList<NetworkLibraryItem> myItems = new LinkedList<NetworkLibraryItem>();
	private final HashMap<INetworkLink, LinkedList<NetworkLibraryItem>> myUncommitedItems = new HashMap<INetworkLink, LinkedList<NetworkLibraryItem>>();
	private final Object myItemsMonitor = new Object();

	private volatile boolean myFinishProcessed;
	private final Object myFinishMonitor = new Object();


	public final void addItem(INetworkLink link, NetworkLibraryItem item) {
		synchronized (myItemsMonitor) {
			myItems.add(item);
			LinkedList<NetworkLibraryItem> uncommited = myUncommitedItems.get(link);
			if (uncommited == null) {
				uncommited = new LinkedList<NetworkLibraryItem>();
				myUncommitedItems.put(link, uncommited);
			}
			uncommited.add(item);
		}
	}

	public final void commitItems(INetworkLink link) {
		synchronized (myItemsMonitor) {
			LinkedList<NetworkLibraryItem> uncommited = myUncommitedItems.get(link);
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

	private final void doUpdateItems() {
		synchronized (myItemsMonitor) {
			onUpdateItems(myItems);
			myItems.clear();
			myItemsMonitor.notifyAll(); // wake up process, that waits for finish condition (see ensureFinish() method)
		}
		afterUpdateItems();
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
		HashSet<NetworkLibraryItem> uncommitedItems = new HashSet<NetworkLibraryItem>();
		synchronized (myUncommitedItems) {
			for (LinkedList<NetworkLibraryItem> items: myUncommitedItems.values()) {
				uncommitedItems.addAll(items);
			}
		}
		synchronized (myFinishMonitor) {
			onFinish(errorMessage, interrupted, uncommitedItems);
			myFinishProcessed = true;
			myFinishMonitor.notifyAll(); // wake up process, that waits for finish condition (see ensureFinish() method)
		}
	}


	// sending messages methods
	public final void sendUpdateItems() {
		sendEmptyMessage(WHAT_UPDATE_ITEMS);
	}

	public final void sendFinish(String errorMessage, boolean interrupted) {
		int arg1 = interrupted ? 1 : 0;
		sendMessage(obtainMessage(WHAT_FINISHED, arg1, 0, errorMessage));
	}


	// callbacks
	public abstract void onUpdateItems(List<NetworkLibraryItem> items);
	public abstract void afterUpdateItems();
	public abstract void onFinish(String errorMessage, boolean interrupted, Set<NetworkLibraryItem> uncommitedItems);


	@Override
	public final void handleMessage(Message message) {
		switch (message.what) {
		case WHAT_UPDATE_ITEMS:
			doUpdateItems();
			break;
		case WHAT_FINISHED:
			doProcessFinish((String) message.obj, message.arg1 != 0);
			break;
		}
	}
}
