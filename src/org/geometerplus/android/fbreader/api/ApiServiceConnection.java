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

package org.geometerplus.android.fbreader.api;

import android.content.*;
import android.os.IBinder;

public class ApiServiceConnection implements ServiceConnection {
	private static String ACTION_API = "android.fbreader.action.API";

	private final Context myContext;
	private volatile ApiInterface myInterface;

	public ApiServiceConnection(Context context) {
		myContext = context;
		connect();
	}

	public synchronized void connect() {
		if (myInterface == null) {
			myContext.bindService(new Intent(ACTION_API), this, Context.BIND_AUTO_CREATE);
		}
	}

	public synchronized void disconnect() {
		if (myInterface != null) {
			try {
				myContext.unbindService(this);
			} catch (IllegalArgumentException e) {
			}
			myInterface = null;
		}
	}

	public synchronized void onServiceConnected(ComponentName className, IBinder service) {
		System.err.println("onServiceConnected call");
		myInterface = ApiInterface.Stub.asInterface(service);
	}

	public synchronized void onServiceDisconnected(ComponentName name) {
		System.err.println("onServiceDisconnected call");
		myInterface = null;
	}

	private void checkConnection() throws ApiException {
		if (myInterface == null) {
			throw new ApiException("Not connected to FBReader");
		}
	}

	public synchronized TextPosition getPageStart() throws ApiException {
		checkConnection();
		try {
			return myInterface.getPageStart();
		} catch (android.os.RemoteException e) {
			throw new ApiException(e);
		}
	}

	public synchronized TextPosition getPageEnd() throws ApiException {
		checkConnection();
		try {
			return myInterface.getPageEnd();
		} catch (android.os.RemoteException e) {
			throw new ApiException(e);
		}
	}

	  public synchronized void setPageStart(TextPosition position) throws ApiException {
		checkConnection();
		try {
			myInterface.setPageStart(position);
		} catch (android.os.RemoteException e) {
			throw new ApiException(e);
		}
	}

	public synchronized int getParagraphsNumber() throws ApiException {
		checkConnection();
		try {
			return myInterface.getParagraphsNumber();
		} catch (android.os.RemoteException e) {
			throw new ApiException(e);
		}
	}

	public synchronized String getParagraphText(int paragraphIndex) throws ApiException {
		checkConnection();
		try {
			return myInterface.getParagraphText(paragraphIndex);
		} catch (android.os.RemoteException e) {
			throw new ApiException(e);
		}
	}
}
