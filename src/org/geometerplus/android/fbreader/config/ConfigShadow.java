/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.util.*;

import android.app.Service;
import android.content.*;
import android.os.IBinder;
import android.os.RemoteException;

import org.geometerplus.zlibrary.core.options.Config;

import org.geometerplus.android.fbreader.api.FBReaderIntents;

public final class ConfigShadow extends Config implements ServiceConnection {
	private final Context myContext;
	private volatile ConfigInterface myInterface;
	private final List<Runnable> myDeferredActions = new LinkedList<Runnable>();

	private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			try {
				setToCache(
					intent.getStringExtra("group"),
					intent.getStringExtra("name"),
					intent.getStringExtra("value")
				);
			} catch (Exception e) {
				// ignore
			}
		}
	};

	public ConfigShadow(Context context) {
		myContext = context;
		context.bindService(
			FBReaderIntents.internalIntent(FBReaderIntents.Action.CONFIG_SERVICE),
			this,
			Service.BIND_AUTO_CREATE
		);
	}

	@Override
	public boolean isInitialized() {
		return myInterface != null;
	}

	@Override
	public void runOnConnect(Runnable runnable) {
		if (myInterface != null) {
			runnable.run();
		} else {
			synchronized (myDeferredActions) {
				myDeferredActions.add(runnable);
			}
		}
	}

	@Override
	public List<String> listGroups() {
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
	public List<String> listNames(String group) {
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
	public void removeGroup(String name) {
		if (myInterface != null) {
			try {
				myInterface.removeGroup(name);
			} catch (RemoteException e) {
			}
		}
	}

	public boolean getSpecialBooleanValue(String name, boolean defaultValue) {
		return myContext.getSharedPreferences("fbreader.ui", Context.MODE_PRIVATE)
			.getBoolean(name, defaultValue);
	}

	public void setSpecialBooleanValue(String name, boolean value) {
		myContext.getSharedPreferences("fbreader.ui", Context.MODE_PRIVATE).edit()
			.putBoolean(name, value).commit();
	}

	public String getSpecialStringValue(String name, String defaultValue) {
		return myContext.getSharedPreferences("fbreader.ui", Context.MODE_PRIVATE)
			.getString(name, defaultValue);
	}

	public void setSpecialStringValue(String name, String value) {
		myContext.getSharedPreferences("fbreader.ui", Context.MODE_PRIVATE).edit()
			.putString(name, value).commit();
	}

	@Override
	protected String getValueInternal(String group, String name) throws NotAvailableException {
		if (myInterface == null) {
			throw new NotAvailableException("Config is not initialized for " + group + ":" + name);
		}
		try {
			return myInterface.getValue(group, name);
		} catch (RemoteException e) {
			throw new NotAvailableException("RemoteException for " + group + ":" + name);
		}
	}

	@Override
	protected void setValueInternal(String group, String name, String value) {
		if (myInterface != null) {
			try {
				myInterface.setValue(group, name, value);
			} catch (RemoteException e) {
			}
		}
	}

	@Override
	protected void unsetValueInternal(String group, String name) {
		if (myInterface != null) {
			try {
				myInterface.unsetValue(group, name);
			} catch (RemoteException e) {
			}
		}
	}

	@Override
	protected Map<String,String> requestAllValuesForGroupInternal(String group) throws NotAvailableException {
		if (myInterface == null) {
			throw new NotAvailableException("Config is not initialized for " + group);
		}
		try {
			final Map<String,String> values = new HashMap<String,String>();
			for (String pair : myInterface.requestAllValuesForGroup(group)) {
				final String[] split = pair.split("\000");
				switch (split.length) {
					case 1:
						values.put(split[0], "");
						break;
					case 2:
						values.put(split[0], split[1]);
						break;
				}
			}
			return values;
		} catch (RemoteException e) {
			throw new NotAvailableException("RemoteException for " + group);
		}
	}

	// method from ServiceConnection interface
	public void onServiceConnected(ComponentName name, IBinder service) {
		synchronized (this) {
			myInterface = ConfigInterface.Stub.asInterface(service);
			myContext.registerReceiver(
				myReceiver, new IntentFilter(FBReaderIntents.Event.CONFIG_OPTION_CHANGE)
			);
		}

		final List<Runnable> actions;
		synchronized (myDeferredActions) {
			actions = new ArrayList<Runnable>(myDeferredActions);
			myDeferredActions.clear();
		}
		for (Runnable a : actions) {
			a.run();
		}
	}

	// method from ServiceConnection interface
	public synchronized void onServiceDisconnected(ComponentName name) {
		myContext.unregisterReceiver(myReceiver);
	}
}
