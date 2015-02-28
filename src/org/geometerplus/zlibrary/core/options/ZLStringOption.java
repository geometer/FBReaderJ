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

public final class ZLStringOption extends ZLOption {
	public ZLStringOption(String group, String optionName, String defaultValue) {
		super(group, optionName, defaultValue);
	}

	public String getValue() {
		if (mySpecialName != null && !Config.Instance().isInitialized()) {
			return Config.Instance().getSpecialStringValue(mySpecialName, myDefaultStringValue);
		} else {
			return getConfigValue();
		}
	}

	public void setValue(String value) {
		if (value == null) {
			return;
		}
		if (mySpecialName != null) {
			Config.Instance().setSpecialStringValue(mySpecialName, value);
		}
		setConfigValue(value);
	}

	public void saveSpecialValue() {
		if (mySpecialName != null && Config.Instance().isInitialized()) {
			Config.Instance().setSpecialStringValue(mySpecialName, getValue());
		}
	}
}
