/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.util.ZLBoolean3;

public final class ZLBoolean3Option extends ZLOption {
	public ZLBoolean3Option(String group, String optionName, ZLBoolean3 defaultValue) {
		super(group, optionName, defaultValue.Name);
	}

	public ZLBoolean3 getValue() {
		return ZLBoolean3.getByName(getConfigValue());
	}

	public void setValue(ZLBoolean3 value) {
		if (value == null) {
			return;
		}
		setConfigValue(value.Name);
	}
}
