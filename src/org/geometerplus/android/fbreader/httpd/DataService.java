/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.httpd;

import java.io.IOException;

import android.app.Service;
import android.content.*;
import android.os.IBinder;
import android.os.RemoteException;

import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;

public class DataService extends Service {
	final AndroidImageSynchronizer ImageSynchronizer = new AndroidImageSynchronizer(this);

	public static class Connection implements ServiceConnection {
		private DataInterface myDataInterface;

		public void onServiceConnected(ComponentName componentName, IBinder binder) {
			myDataInterface = DataInterface.Stub.asInterface(binder);
		}

		public void onServiceDisconnected(ComponentName componentName) {
			myDataInterface = null;
		}

		public int getPort() {
			try {
				return myDataInterface != null ? myDataInterface.getPort() : -1;
			} catch (RemoteException e) {
				return -1;
			}
		}
	}

	private DataServer myServer;
	private volatile int myPort = -1;

	@Override
	public void onCreate() {
		new Thread(new Runnable() {
			public void run () {
				for (int port = 12000; port < 12500; ++port) {
					try {
						myServer = new DataServer(DataService.this, port);
						myServer.start();
						myPort = port;
						break;
					} catch (IOException e) {
						myServer = null;
					}
				}
			}
		}).start();
	}

	@Override
	public void onDestroy() {
		if (myServer != null) {
			new Thread(new Runnable() {
				public void run () {
					if (myServer != null) {
						myServer.stop();
						myServer = null;
					}
				}
			}).start();
		}
		ImageSynchronizer.clear();
		super.onDestroy();
	}

	public IBinder onBind(Intent intent) {
		return new DataInterface.Stub() {
			public int getPort() {
				return myPort;
			}
		};
	}
}
