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

package org.geometerplus.fbreader.network;

import java.util.*;

public abstract class NetworkItemsLoader<T extends NetworkTree> implements Runnable {
	private final T myTree;

	protected NetworkItemsLoader(T tree) {
		myTree = tree;
	}

	public final void start() {
		final Thread loaderThread = new Thread(this);
		loaderThread.setPriority(Thread.MIN_PRIORITY);
		loaderThread.start();
	}

	protected T getTree() {
		return myTree;
	}

	public abstract void setPostRunnable(Runnable runnable);
	protected abstract void addItem(NetworkItem item);

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

	public final void interrupt() {
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

	private final List<NetworkItem> myUncommitedItems =
		Collections.synchronizedList(new LinkedList<NetworkItem>());

	protected final Set<NetworkItem> uncommitedItems() {
		synchronized (myUncommitedItems) {
			return new HashSet<NetworkItem>(myUncommitedItems);
		}
	}

	public final void onNewItem(final NetworkItem item) {
		myUncommitedItems.add(item);
		addItem(item);
	}

	public final void commitItems() {
		myUncommitedItems.clear();
	}
}
