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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;

import org.geometerplus.fbreader.network.NetworkTree;

public class ItemsLoadingService extends Service {
	private static final String KEY = "ItemsLoadingRunnable";

	static void start(Context context, NetworkTree tree, ItemsLoader runnable) {
		boolean doDownload = false;
		synchronized (tree) {
			if (tree.getUserData(KEY) == null) {
				tree.setUserData(KEY, runnable);
				doDownload = true;
			}
		}
		if (doDownload) {
			context.startService(
				new Intent(context.getApplicationContext(), ItemsLoadingService.class)
					.putExtra(Util.TREE_KEY_KEY, tree.getUniqueKey())
			);
		}
	}

	static ItemsLoader getRunnable(NetworkTree tree) {
		return (ItemsLoader)tree.getUserData(KEY);
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

	private volatile int myServiceCounter;

	private void doStart() {
		++myServiceCounter;
	}

	private void doStop() {
		if (--myServiceCounter == 0) {
			stopSelf();
		}
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		doStart();

		final NetworkTree tree = Util.getTreeFromIntent(intent);
		if (tree == null) {
			doStop();
			return;
		}
		intent.removeExtra(Util.TREE_KEY_KEY);

		if (!NetworkView.Instance().isInitialized()) {
			doStop();
			return;
		}

		final ItemsLoader runnable = getRunnable(tree);
		if (runnable == null) {
			doStop();
			return;
		}

		final Handler finishHandler = new Handler() {
			public void handleMessage(Message message) {
				doStop();
				removeRunnable(tree);
				NetworkView.Instance().fireModelChanged();
			}
		};

		// this call is needed to show indeterminate progress bar in title right on downloading start
		NetworkView.Instance().fireModelChangedAsync();

		final Thread loader = new Thread(new Runnable() {
			public void run() {
				try {
					runnable.run();
				} finally {
					finishHandler.sendEmptyMessage(0);
				}
			}
		});
		loader.setPriority(Thread.MIN_PRIORITY);
		loader.start();
	}
}
