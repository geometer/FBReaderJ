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

import java.util.Set;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;

public abstract class ItemsLoader extends NetworkItemsLoader {
	private volatile boolean myFinished;
	private volatile Runnable myPostRunnable;
	private final Object myFinishedLock = new Object();

	public ItemsLoader(NetworkCatalogTree tree) {
		super(tree);
	}

	public final void run() {
		final NetworkLibrary library = NetworkLibrary.Instance();

		try {
			library.storeLoader(getTree(), this);
			library.fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);

			try {
				doBefore();
			} catch (ZLNetworkException e) {
				onFinish(e.getMessage(), false);
				return;
			}
			String error = null;
			try {
				doLoading();
			} catch (ZLNetworkException e) {
				error = e.getMessage();
			}

			onFinish(error, isLoadingInterrupted());
		} finally {
			library.removeStoredLoader(getTree());
			synchronized (myFinishedLock) {
				if (myPostRunnable != null) {
					myPostRunnable.run();
				}
				myFinished = true;
			}
			library.fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
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

	protected abstract void onFinish(String errorMessage, boolean interrupted);

	protected abstract void doBefore() throws ZLNetworkException;
	protected abstract void doLoading() throws ZLNetworkException;
}
