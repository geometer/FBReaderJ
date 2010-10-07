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

import android.os.Message;
import android.os.Handler;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkOperationData;
import org.geometerplus.fbreader.network.NetworkLibraryItem;


abstract class ItemsLoadingRunnable implements Runnable {

	private final ItemsLoadingHandler myHandler;

	private final long myUpdateInterval; // in milliseconds
	private final int myItemsLimit;

	private boolean myInterruptRequested;
	private boolean myInterruptConfirmed;
	private Object myInterruptLock = new Object();

	private boolean myFinished;
	private Handler myFinishedHandler;
	private Object myFinishedLock = new Object();


	public void interruptLoading() {
		synchronized (myInterruptLock) {
			myInterruptRequested = true;
		}
	}

	private boolean confirmInterruptLoading() {
		synchronized (myInterruptLock) {
			if (myInterruptRequested) {
				myInterruptConfirmed = true;
			}
			return myInterruptConfirmed;
		}
	}

	public boolean tryResumeLoading() {
		synchronized (myInterruptLock) {
			if (!myInterruptConfirmed) {
				myInterruptRequested = false;
			}
			return !myInterruptRequested;
		}
	}

	private boolean isLoadingInterrupted() {
		synchronized (myInterruptLock) {
			return myInterruptConfirmed;
		}
	}


	public ItemsLoadingRunnable(ItemsLoadingHandler handler) {
		this(handler, 1000, 500);
	}

	public ItemsLoadingRunnable(ItemsLoadingHandler handler, long updateIntervalMillis, int itemsLimit) {
		myHandler = handler;
		myUpdateInterval = updateIntervalMillis;
		myItemsLimit = itemsLimit;
	}

	public abstract String doBefore();
	public abstract String doLoading(NetworkOperationData.OnNewItemListener doWithListener);

	public abstract String getResourceKey();


	public final void run() {
		String err = doBefore();
		if (err != null) {
			myHandler.sendFinish(err, false);
			return;
		}
		err = doLoading(new NetworkOperationData.OnNewItemListener() {
			private long myUpdateTime;
			private int myItemsNumber;
			public void onNewItem(INetworkLink link, NetworkLibraryItem item) {
				myHandler.addItem(link, item);
				++myItemsNumber;
				final long now = System.currentTimeMillis();
				if (now > myUpdateTime) {
					myHandler.sendUpdateItems();
					myUpdateTime = now + myUpdateInterval;
				}
			}
			public boolean confirmInterrupt() {
				return confirmInterruptLoading() /*|| myItemsNumber >= ItemsLimit*/;
			}
			public void commitItems(INetworkLink link) {
				myHandler.commitItems(link);
			}
		});
		myHandler.sendUpdateItems();
		myHandler.ensureItemsProcessed();
		myHandler.sendFinish(err, isLoadingInterrupted());
		myHandler.ensureFinishProcessed();
	}

	void runFinishHandler() {
		synchronized (myFinishedLock) {
			if (myFinishedHandler != null) {
				myFinishedHandler.sendEmptyMessage(0);
			}
			myFinished = true;
		}
	}


	public void runOnFinish(final Runnable runnable) {
		if (myFinishedHandler != null) {
			return;
		}
		synchronized (myFinishedLock) {
			if (myFinished) {
				runnable.run();
			} else {
				myFinishedHandler = new Handler() {
					public void handleMessage(Message message) {
						runnable.run();
					}
				};
			}
		}
	}
}
