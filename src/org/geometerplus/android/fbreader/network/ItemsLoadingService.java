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

import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.app.Service;
import android.content.Intent;


public class ItemsLoadingService extends Service {

	public static final String ITEMS_LOADING_RUNNABLE_KEY = "org.geometerplus.android.fbreader.network.ItemsLoadingRunnable";

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

		final String key = intent.getStringExtra(ITEMS_LOADING_RUNNABLE_KEY);
		if (key == null) {
			doStop();
			return;
		}
		intent.removeExtra(ITEMS_LOADING_RUNNABLE_KEY);

		if (!NetworkView.Instance().isInitialized()) {
			doStop();
			return;
		}
		final ItemsLoadingRunnable runnable = NetworkView.Instance().getItemsLoadingRunnable(key);
		if (runnable == null) {
			doStop();
			return;
		}

		final Handler finishHandler = new Handler() {
			public void handleMessage(Message message) {
				doStop();
				if (NetworkView.Instance().isInitialized()) {
					NetworkView.Instance().removeItemsLoadingRunnable(key);
					NetworkView.Instance().fireModelChanged();
				}
			}
		};

		NetworkView.Instance().fireModelChanged(); // this call is needed to show indeterminate progress bar in title right on downloading start

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
