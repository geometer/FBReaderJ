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

public final class ZLBooleanOption extends ZLOption {
	private final boolean myDefaultValue;

	public ZLBooleanOption(String group, String optionName, boolean defaultValue) {
		super(group, optionName, defaultValue ? "true" : "false");
		myDefaultValue = defaultValue;
	}

	public boolean getValue() {
		if (mySpecialName != null && !Config.Instance().isInitialized()) {
			return Config.Instance().getSpecialBooleanValue(mySpecialName, myDefaultValue);
		} else {
			return "true".equals(getConfigValue());
		}
	}

	public void setValue(boolean value) {
		if (mySpecialName != null) {
			Config.Instance().setSpecialBooleanValue(mySpecialName, value);
		}
		setConfigValue(value ? "true" : "false");
	}

	public void saveSpecialValue() {
		if (mySpecialName != null && Config.Instance().isInitialized()) {
			Config.Instance().setSpecialBooleanValue(mySpecialName, getValue());
		}
	}
}
