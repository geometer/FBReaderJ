/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.config;

public abstract class ZLConfig {
	public static ZLConfig Instance() {
		return ourInstance;
	}

	private static ZLConfig ourInstance;

	protected ZLConfig() {
		ourInstance = this;
	}

	public abstract String getValue(String group, String name, String defaultValue);
	public abstract void setValue(String group, String name, String value);
	public abstract void unsetValue(String group, String name);
	public abstract void removeGroup(String name);
}
