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

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Service;
import android.content.Intent;


public class LibraryInitializationService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		final NetworkView view = NetworkView.Instance();
		if (!view.isInitialized()) {
			stopSelf();
			return;
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what > 0) {
					view.finishBackgroundUpdate();
				}
				stopSelf();
			}
		};

		final Thread thread = new Thread(new Runnable() {
			public void run() {
				boolean result = false; 
				try {
					result = view.runBackgroundUpdate();
				} finally {
					handler.sendEmptyMessage(result ? 1 : 0);
				}
			}
		});
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
}
