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

import java.util.concurrent.atomic.AtomicBoolean;

import org.geometerplus.fbreader.network.NetworkOperationData;
import org.geometerplus.fbreader.network.NetworkLibraryItem;


abstract class ItemsLoadingRunnable implements Runnable {

	public final AtomicBoolean InterruptFlag = new AtomicBoolean();

	private final ItemsLoadingHandler myHandler;

	private final long myUpdateInterval; // in milliseconds
	private final int myItemsLimit;

	public ItemsLoadingRunnable(ItemsLoadingHandler handler) {
		this(handler, 1000, 250);
	}

	public ItemsLoadingRunnable(ItemsLoadingHandler handler, long updateIntervalMillis, int itemsLimit) {
		myHandler = handler;
		myUpdateInterval = updateIntervalMillis;
		myItemsLimit = itemsLimit;
	}

	public abstract String doBefore();
	public abstract String doLoading(NetworkOperationData.OnNewItemListener doWithListener);

	public final void run() {
		String err = doBefore();
		if (err != null) {
			myHandler.sendFinish(err);
			return;
		}
		err = doLoading(new NetworkOperationData.OnNewItemListener() {
			private long myUpdateTime;
			private int myItemsNumber;
			public boolean onNewItem(NetworkLibraryItem item) {
				myHandler.addItem(item);
				if (InterruptFlag.get() || myItemsNumber++ >= myItemsLimit) {
					return true;
				}
				final long now = System.currentTimeMillis();
				if (now > myUpdateTime) {
					myHandler.sendUpdateItems();
					myUpdateTime = now + myUpdateInterval;
				}
				return false;
			}
		});
		myHandler.sendUpdateItems();
		myHandler.ensureFinish();
		myHandler.sendFinish(err);
	}
}
