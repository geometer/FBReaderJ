/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

public abstract class ZLConfigManager {
	private static ZLConfigManager ourInstance;
	private static ZLConfig ourConfig;

	// TODO: remove this method
	public static ZLConfigManager getInstance() {
		return ourInstance;
	}

	public static ZLConfig getConfig() {
		return ourConfig;
	}

	protected static void setConfig(ZLConfig config) {
		ourConfig = config;
	}

	public static void release() {
		if (ourInstance != null) {
			ourInstance.shutdown();
			ourInstance = null;
			ourConfig = null;
		}
	}

	protected abstract void shutdown();

	// TODO: remove these methods
	public abstract void saveAll();
	public abstract void saveDelta();

	protected ZLConfigManager() {
		ourInstance = this;
	}
}
