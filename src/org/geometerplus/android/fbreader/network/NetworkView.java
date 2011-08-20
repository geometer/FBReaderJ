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
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.Menu;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;

public class NetworkView {
	private static NetworkView ourInstance;

	public static NetworkView Instance() {
		if (ourInstance == null) {
			ourInstance = new NetworkView();
		}
		return ourInstance;
	}

	private volatile boolean myInitialized;

	private NetworkView() {
	}

	public boolean isInitialized() {
		return myInitialized;
	}

	public void initialize() throws ZLNetworkException {
		new SQLiteNetworkDatabase();

		final NetworkLibrary library = NetworkLibrary.Instance();
		library.initialize();
		library.synchronize();

		myInitialized = true;
	}

	public void runBackgroundUpdate(boolean clearCache) throws ZLNetworkException {
		NetworkLibrary.Instance().runBackgroundUpdate(clearCache);
	}

	// This method MUST be called from main thread
	// This method has effect only when runBackgroundUpdate method has returned null
	public void finishBackgroundUpdate() {
		NetworkLibrary library = NetworkLibrary.Instance();
		library.finishBackgroundUpdate();
		library.synchronize();
		fireModelChanged();
	}

	/*
	 * Code for loading network items (running items-loading service and managing items-loading runnables).
	 */

	public void tryResumeLoading(Activity activity, NetworkCatalogTree tree, Runnable expandRunnable) {
		final ItemsLoader runnable = ItemsLoadingService.getRunnable(tree);
		if (runnable != null && runnable.tryResumeLoading()) {
			Util.openTree(activity, tree);
			return;
		}
		if (runnable == null) {
			expandRunnable.run();
		} else {
			runnable.runOnFinish(expandRunnable);
		}
	}


	/*
	 * Notifying view's components from services
	 */

	public interface EventListener {
		void onModelChanged();
	}

	private Handler myEventHandler;
	private LinkedList<EventListener> myEventListeners = new LinkedList<EventListener>();

	/*
	 * This method must be called only from main thread
	 */
	public final void addEventListener(EventListener listener) {
		synchronized (myEventListeners) {
			if (myEventHandler == null) {
				myEventHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						fireModelChanged();
					}
				};
			}
			if (listener != null) {
				myEventListeners.add(listener);
			}
		}
	}

	public final void removeEventListener(EventListener listener) {
		synchronized (myEventListeners) {
			myEventListeners.remove(listener);
		}
	}

	public final void fireModelChangedAsync() {
		synchronized (myEventListeners) {
			if (myEventHandler != null) {
				myEventHandler.sendEmptyMessage(0);
			}
		}
	}

	public final void fireModelChanged() {
		for (EventListener listener : myEventListeners) {
			listener.onModelChanged();
		}
	}
}
