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

package org.geometerplus.zlibrary.core.options;

import org.geometerplus.zlibrary.core.config.ZLConfig;

public abstract class ZLOption {
	public static final String LOOK_AND_FEEL_CATEGORY = "ui";
	public static final String CONFIG_CATEGORY = "options";
	public static final String STATE_CATEGORY = "state";
	public static final String EMPTY = "";
	public static final String PLATFORM_GROUP = "PlatformOptions";
	
	private final String myCategory;
	private final String myGroup;
	private String myOptionName;
	protected boolean myIsSynchronized;

	/**
	 * конструктор
	 * 
	 * @param config
	 * @param category
	 * @param group
	 * @param optionName
	 */
	protected ZLOption(String category, String group, String optionName) {
		myCategory = category.intern();
		myGroup = group.intern();
		myOptionName = optionName.intern();
		myIsSynchronized = false;
	}

	protected void changeName(String optionName) {
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
			config.setValue(myGroup, myOptionName, value, myCategory);
		}
	}

	protected final void unsetConfigValue() {
		ZLConfig config = ZLConfig.Instance();
		if (config != null) {
			config.unsetValue(myGroup, myOptionName);
		}
	}
}
