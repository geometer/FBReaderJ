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

package org.geometerplus.zlibrary.core.options;

import org.geometerplus.zlibrary.core.config.ZLConfig;

public abstract class ZLOption {
	public static final String PLATFORM_GROUP = "PlatformOptions";
	
	private final String myGroup;
	private final String myOptionName;
	protected boolean myIsSynchronized;

	protected ZLOption(String group, String optionName) {
		myGroup = group.intern();
		myOptionName = optionName.intern();
		myIsSynchronized = false;
	}

	protected final String getConfigValue(String defaultValue) {
		ZLConfig config = ZLConfig.Instance();
		return (config != null) ?
			config.getValue(myGroup, myOptionName, defaultValue) : defaultValue;
	}

	protected final void setConfigValue(String value) {
		ZLConfig config = ZLConfig.Instance();
		if (config != null) {
			config.setValue(myGroup, myOptionName, value);
		}
	}

	protected final void unsetConfigValue() {
		ZLConfig config = ZLConfig.Instance();
		if (config != null) {
			config.unsetValue(myGroup, myOptionName);
		}
	}
}
