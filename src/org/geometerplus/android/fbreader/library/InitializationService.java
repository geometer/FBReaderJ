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

package org.geometerplus.android.fbreader.library;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.HandlerThread;
import android.os.Handler;
import android.os.Message;

public class InitializationService extends Service {
	private static volatile HandlerThread myLibraryInitializer;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		System.err.println("onStart");
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				stopSelf();
			}
		};

		myLibraryInitializer = new HandlerThread("LibraryInitializer") {
			public void run() {
				try {
					LibraryBaseActivity.LibraryInstance.synchronize();
				} finally {
					myLibraryInitializer = null;
					handler.sendMessage(handler.obtainMessage(0));
				}
			}
		};
		myLibraryInitializer.setPriority(Thread.MIN_PRIORITY);
		myLibraryInitializer.start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		onStart(intent, startId);
		return 0;
	}

	@Override
	public void onDestroy() {
		System.err.println("onDestroy");
		if (myLibraryInitializer != null) {
			try {
				myLibraryInitializer.getLooper().quit();
			} catch (Exception e) {
			}
		}
		super.onDestroy();
	}
}
