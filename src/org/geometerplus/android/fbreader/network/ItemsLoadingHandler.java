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

import android.os.Message;
import android.os.Handler;

import org.geometerplus.fbreader.network.NetworkLibraryItem;


abstract class ItemsLoadingHandler extends Handler {

	private static final int WHAT_UPDATE_ITEMS = 0;
	private static final int WHAT_FINISHED = 1;

	private final LinkedList<NetworkLibraryItem> myItems = new LinkedList<NetworkLibraryItem>();
	private final Object myItemsMonitor = new Object();

	public final void addItem(NetworkLibraryItem item) {
		synchronized (myItemsMonitor) {
			myItems.add(item);
		}
	}

	public final void ensureFinish() {
		synchronized (myItemsMonitor) {
			while (myItems.size() > 0) {
				try {
					myItemsMonitor.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public final void sendUpdateItems() {
		sendEmptyMessage(WHAT_UPDATE_ITEMS);
	}

	public final void sendFinish(String errorMessage) {
		sendMessage(obtainMessage(WHAT_FINISHED, errorMessage));
	}

	public abstract void onUpdateItems(List<NetworkLibraryItem> items);
	public abstract void afterUpdateItems();
	public abstract void onFinish(String errorMessage);

	private final void doUpdateItems() {
		synchronized (myItemsMonitor) {
			onUpdateItems(myItems);
			myItems.clear();
			myItemsMonitor.notifyAll(); // wake up process, that waits for all items to be displayed (see ensureFinish() method)
		}
		afterUpdateItems();
	}

	@Override
	public final void handleMessage(Message message) {
		switch (message.what) {
		case WHAT_UPDATE_ITEMS:
			doUpdateItems();
			break;
		case WHAT_FINISHED:
			onFinish((String) message.obj);
			break;
		}
	}
}
