/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network.tree;

import org.geometerplus.zlibrary.core.network.ZLNetworkContext;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkItem;

public abstract class NetworkItemsLoader implements Runnable {
	private final NetworkCatalogTree myTree;

	private volatile Runnable myPostRunnable;
	private volatile boolean myFinishedFlag;
	public final ZLNetworkContext NetworkContext;

	protected NetworkItemsLoader(ZLNetworkContext nc, NetworkCatalogTree tree) {
		NetworkContext = nc;
		myTree = tree;
	}

	public final void start() {
		final Thread loaderThread = new Thread(this);
		loaderThread.setPriority(Thread.MIN_PRIORITY);
		loaderThread.start();
	}

	public NetworkCatalogTree getTree() {
		return myTree;
	}

	public final void run() {
		final NetworkLibrary library = NetworkLibrary.Instance();

		synchronized (library) {
			final NetworkCatalogTree tree = getTree();
			if (library.isLoadingInProgress(tree)) {
				return;
			}
			library.storeLoader(tree, this);
		}

		try {
			library.fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);

			try {
				doBefore();
			} catch (ZLNetworkException e) {
				onFinish(e, false);
				return;
			}

			try {
				load();
				onFinish(null, isLoadingInterrupted());
			} catch (ZLNetworkException e) {
				onFinish(e, isLoadingInterrupted());
			}
		} finally {
			library.removeStoredLoader(getTree());
			library.fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
			synchronized (this) {
				if (myPostRunnable != null) {
					myPostRunnable.run();
					myFinishedFlag = true;
				}
			}
		}
	}

	private final Object myInterruptLock = new Object();
	private enum InterruptionState {
		NONE,
		REQUESTED,
		CONFIRMED
	};
	private InterruptionState myInterruptionState = InterruptionState.NONE;

	public final boolean canResumeLoading() {
		synchronized (myInterruptLock) {
			if (myInterruptionState == InterruptionState.REQUESTED) {
				myInterruptionState = InterruptionState.NONE;
			}
			return myInterruptionState == InterruptionState.NONE;
		}
	}

	protected final boolean isLoadingInterrupted() {
		synchronized (myInterruptLock) {
			return myInterruptionState == InterruptionState.CONFIRMED;
		}
	}

	public void interrupt() {
		synchronized (myInterruptLock) {
			if (myInterruptionState == InterruptionState.NONE) {
				myInterruptionState = InterruptionState.REQUESTED;
			}
		}
	}

	public final boolean confirmInterruption() {
		synchronized (myInterruptLock) {
			if (myInterruptionState == InterruptionState.REQUESTED) {
				myInterruptionState = InterruptionState.CONFIRMED;
			}
			return myInterruptionState == InterruptionState.CONFIRMED;
		}
	}

	public void onNewItem(final NetworkItem item) {
		getTree().addItem(item);
	}

	public synchronized void setPostRunnable(Runnable action) {
		if (myFinishedFlag) {
			action.run();
		} else {
			myPostRunnable = action;
		}
	}

	protected abstract void onFinish(ZLNetworkException exception, boolean interrupted);
	protected abstract void doBefore() throws ZLNetworkException;
	protected abstract void load() throws ZLNetworkException;
}
