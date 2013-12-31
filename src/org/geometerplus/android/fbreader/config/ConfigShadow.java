/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.config;

import java.util.Collections;
import java.util.List;

import android.content.*;
import android.os.IBinder;
import android.os.RemoteException;

import org.geometerplus.zlibrary.core.config.ZLConfig;

public final class ConfigShadow extends ZLConfig implements ServiceConnection {
	private volatile ConfigInterface myInterface;

	public ConfigShadow(Context context) {
		context.bindService(
			new Intent(context, ConfigService.class),
			this,
			ConfigService.BIND_AUTO_CREATE
		);
	}

	@Override
	synchronized public List<String> listGroups() {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return myInterface.listGroups();
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	@Override
	synchronized public List<String> listNames(String group) {
		if (myInterface == null) {
			return Collections.emptyList();
		}
		try {
			return myInterface.listNames(group);
		} catch (RemoteException e) {
			return Collections.emptyList();
		}
	}

	@Override
	synchronized public void removeGroup(String name) {
		if (myInterface != null) {
			try {
				myInterface.removeGroup(name);
			} catch (RemoteException e) {
			}
		}
	}

	@Override
	synchronized public String getValue(String group, String name, String defaultValue) {
		if (myInterface == null) {
			return defaultValue;
		}
		try {
			return myInterface.getValue(group, name, defaultValue);
		} catch (RemoteException e) {
			return defaultValue;
		}
	}

	@Override
	synchronized public void setValue(String group, String name, String value) {
		if (myInterface != null) {
			try {
				myInterface.setValue(group, name, value);
			} catch (RemoteException e) {
			}
		}
	}

	@Override
	synchronized public void unsetValue(String group, String name) {
		if (myInterface != null) {
			try {
				myInterface.unsetValue(group, name);
			} catch (RemoteException e) {
			}
		}
	}

	// method from ServiceConnection interface
	public synchronized void onServiceConnected(ComponentName name, IBinder service) {
		myInterface = ConfigInterface.Stub.asInterface(service);
	}

	// method from ServiceConnection interface
	public synchronized void onServiceDisconnected(ComponentName name) {
	}
}
