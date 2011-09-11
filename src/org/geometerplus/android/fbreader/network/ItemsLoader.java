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

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;

public abstract class ItemsLoader<T extends NetworkTree> extends NetworkItemsLoader<T> {
	protected final Activity myActivity;

	private volatile int myItemsCounter = 0;
	private final LinkedList<NetworkItem> myUncommitedItems = new LinkedList<NetworkItem>();
	private final Object myItemsMonitor = new Object();

	private volatile boolean myFinishProcessed;
	private final Object myFinishMonitor = new Object();

	private volatile boolean myFinished;
	private volatile Runnable myPostRunnable;
	private final Object myFinishedLock = new Object();

	public ItemsLoader(Activity activity, T tree) {
		super(tree);
		myActivity = activity;
	}

	public void start() {
		final NetworkLibrary networkLibrary = NetworkLibrary.Instance();

		if (!networkLibrary.isInitialized()) {
			return;
		}

		networkLibrary.storeLoader(getTree(), this);

		// this call is needed to show indeterminate progress bar in title right on downloading start
		networkLibrary.fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);

		final Thread loaderThread = new Thread(new Runnable() {
			public void run() {
				try {
					ItemsLoader.this.run();
				} finally {
					networkLibrary.removeStoredLoader(getTree());
					runFinishHandler();
					networkLibrary.fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
				}
			}
		});
		loaderThread.setPriority(Thread.MIN_PRIORITY);
		loaderThread.start();
	}

	public final void run() {
		try {
			doBefore();
		} catch (ZLNetworkException e) {
			finishOnUiThread(e.getMessage(), false);
			return;
		}
		String error = null;
		try {
			doLoading();
		} catch (ZLNetworkException e) {
			error = e.getMessage();
		}

		ensureItemsProcessed();
		finishOnUiThread(error, isLoadingInterrupted());
		ensureFinishProcessed();
	}

	void runFinishHandler() {
		synchronized (myFinishedLock) {
			if (myPostRunnable != null) {
				myActivity.runOnUiThread(myPostRunnable);
			}
			myFinished = true;
		}
	}

	public void setPostRunnable(Runnable runnable) {
		if (myPostRunnable != null) {
			return;
		}
		synchronized (myFinishedLock) {
			if (myFinished) {
				runnable.run();
			} else {
				myPostRunnable = runnable;
			}
		}
	}

	private final void ensureItemsProcessed() {
		synchronized (myItemsMonitor) {
			while (myItemsCounter > 0) {
				try {
					myItemsMonitor.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private final void ensureFinishProcessed() {
		synchronized (myFinishMonitor) {
			while (!myFinishProcessed) {
				try {
					myFinishMonitor.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private final void finishOnUiThread(final String errorMessage, final boolean interrupted) {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				final Set<NetworkItem> uncommitedItems;
				synchronized (myItemsMonitor) {
					uncommitedItems = new HashSet<NetworkItem>(myUncommitedItems);
				}
				synchronized (myFinishMonitor) {
					onFinish(errorMessage, interrupted, uncommitedItems);
					myFinishProcessed = true;
					// wake up process, that waits for finish condition (see ensureFinish() method)
					myFinishMonitor.notifyAll();
				}
			}
		});
	}

	protected abstract void onFinish(String errorMessage, boolean interrupted, Set<NetworkItem> uncommitedItems);

	protected abstract void addItem(NetworkItem item);

	public abstract void doBefore() throws ZLNetworkException;
	public abstract void doLoading() throws ZLNetworkException;

	public void onNewItem(final NetworkItem item) {
		synchronized (myItemsMonitor) {
			++myItemsCounter;
			myUncommitedItems.add(item);
		}
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				synchronized (myItemsMonitor) {
					addItem(item);
					--myItemsCounter;
					// wake up process, that waits for finish condition (see ensureFinish() method)
					myItemsMonitor.notifyAll();
				}
			}
		});
	}

	public void commitItems() {
		synchronized (myItemsMonitor) {
			myUncommitedItems.clear();
		}
	}
}
