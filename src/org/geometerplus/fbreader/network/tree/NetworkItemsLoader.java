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

package org.geometerplus.fbreader.network.tree;

import java.util.*;

import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkItem;

public abstract class NetworkItemsLoader implements Runnable {
	private final NetworkCatalogTree myTree;

	protected NetworkItemsLoader(NetworkCatalogTree tree) {
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

	public abstract void setPostRunnable(Runnable runnable);

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

	public void onNewItem(final NetworkItem item) {
		getTree().addItem(item);
	}
}
