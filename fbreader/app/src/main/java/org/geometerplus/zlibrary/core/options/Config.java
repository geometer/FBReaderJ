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

package org.geometerplus.zlibrary.core.options;

import java.util.*;

public abstract class Config {
	protected final static class NotAvailableException extends Exception {
		public NotAvailableException(String message) {
			super(message);
		}
	}

	public static Config Instance() {
		return ourInstance;
	}

	private static Config ourInstance;

	protected Config() {
		ourInstance = this;
	}

	private final String myNullString = new String("__NULL__");
	private final Map<StringPair,String> myCache =
		Collections.synchronizedMap(new HashMap<StringPair,String>());
	private final Set<String> myCachedGroups = new HashSet<String>();

	public final String getValue(StringPair id, String defaultValue) {
		String value = myCache.get(id);
		if (value == null) {
			if (myCachedGroups.contains(id.Group)) {
				value = myNullString;
			} else {
				try {
					value = getValueInternal(id.Group, id.Name);
				} catch (NotAvailableException e) {
					return defaultValue;
				}
				if (value == null) {
					value = myNullString;
				}
			}
			myCache.put(id, value);
		}
		return value != myNullString ? value : defaultValue;
	}

	public final void setValue(StringPair id, String value) {
		final String oldValue = myCache.get(id);
		if (oldValue != null && oldValue.equals(value)) {
			return;
		}
		myCache.put(id, value);
		setValueInternal(id.Group, id.Name, value);
	}

	public final void requestAllValuesForGroup(String group) {
		synchronized (myCachedGroups) {
			if (myCachedGroups.contains(group)) {
				return;
			}
			final Map<String,String> values;
			try {
				values = requestAllValuesForGroupInternal(group);
			} catch (NotAvailableException e) {
				return;
			}
			for (Map.Entry<String,String> entry : values.entrySet()) {
				setToCache(group, entry.getKey(), entry.getValue());
			}
			myCachedGroups.add(group);
		}
	}

	public final void unsetValue(StringPair id) {
		myCache.put(id, myNullString);
		unsetValueInternal(id.Group, id.Name);
	}

	protected final void setToCache(String group, String name, String value) {
		myCache.put(new StringPair(group, name), value != null ? value : myNullString);
	}

	public abstract boolean isInitialized();
	public abstract void runOnConnect(Runnable runnable);

	public abstract List<String> listGroups();
	public abstract List<String> listNames(String group);
	public abstract void removeGroup(String name);

	public abstract boolean getSpecialBooleanValue(String name, boolean defaultValue);
	public abstract void setSpecialBooleanValue(String name, boolean value);
	public abstract String getSpecialStringValue(String name, String defaultValue);
	public abstract void setSpecialStringValue(String name, String value);

	protected abstract String getValueInternal(String group, String name) throws NotAvailableException;
	protected abstract void setValueInternal(String group, String name, String value);
	protected abstract void unsetValueInternal(String group, String name);
	protected abstract Map<String,String> requestAllValuesForGroupInternal(String group) throws NotAvailableException;
}
