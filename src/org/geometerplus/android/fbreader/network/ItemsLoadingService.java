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

import android.content.Context;

import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkTree;

public class ItemsLoadingService {
	private static final String KEY = "ItemsLoadingRunnable";

	public static void start(Context context, final NetworkTree tree, final ItemsLoader runnable) {
		final NetworkLibrary networkLibrary = NetworkLibrary.Instance();

		if (!networkLibrary.isInitialized()) {
			return;
		}

		synchronized (tree) {
			if (tree.getUserData(KEY) != null) {
				return;
			}
			tree.setUserData(KEY, runnable);
		}
		// this call is needed to show indeterminate progress bar in title right on downloading start
		networkLibrary.fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);

		final Thread loader = new Thread(new Runnable() {
			public void run() {
				try {
					runnable.run();
				} finally {
					removeRunnable(tree);
					networkLibrary.fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
				}
			}
		});
		loader.setPriority(Thread.MIN_PRIORITY);
		loader.start();
	}

	public static ItemsLoader getRunnable(NetworkTree tree) {
		return tree != null ? (ItemsLoader)tree.getUserData(KEY) : null;
	}

	private static void removeRunnable(NetworkTree tree) {
		synchronized (tree) {
			ItemsLoader runnable = (ItemsLoader)tree.getUserData(KEY);
			if (runnable != null) {
				tree.setUserData(KEY, null);
				runnable.runFinishHandler();
			}
		}
	}
}
