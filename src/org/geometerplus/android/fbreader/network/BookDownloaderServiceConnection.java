/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import android.content.*;
import android.os.IBinder;

public class BookDownloaderServiceConnection implements ServiceConnection {
	private volatile Runnable myAction;
	private volatile BookDownloaderInterface myInterface;

	synchronized void bindToService(Context context, Runnable action) {
		if (myInterface != null) {
			if (action != null) {
				action.run();
			}
		} else {
			myAction = action;
			context.bindService(
				new Intent(context, BookDownloaderService.class),
				this,
				Context.BIND_AUTO_CREATE
			);
		}
	}

	synchronized void unbind(Context context) {
		myAction = null;
		if (myInterface != null) {
			context.unbindService(this);
			myInterface = null;
		}
	}

	public synchronized void onServiceConnected(ComponentName className, IBinder service) {
		myInterface = BookDownloaderInterface.Stub.asInterface(service);
		if (myAction != null) {
			myAction.run();
			myAction = null;
		}
	}

	public synchronized void onServiceDisconnected(ComponentName name) {
		myInterface = null;
	}

	public synchronized boolean isBeingDownloaded(String url) {
		if (myInterface != null) {
			try {
				return myInterface.isBeingDownloaded(url);
			} catch (android.os.RemoteException e) {
			}
		}
		return false;
	}
}
