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

package org.geometerplus.zlibrary.core.application;

import java.util.*;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidKeyUtil;

public final class ZLKeyBindings {
	private final HashMap<Integer, ZLStringOption> myBindingsMap =
		new HashMap<Integer, ZLStringOption>();

	public ZLKeyBindings() {
	}

	public void addKey(int keyId, String defaultActionId) {
		String key = ZLAndroidKeyUtil.getKeyNameByCode(keyId);
		ZLStringOption opt =
			new ZLStringOption("Keys", key, defaultActionId);
		myBindingsMap.put(keyId, opt);
	}

	public String getBinding(int keyId) {
		ZLStringOption action = myBindingsMap.get(keyId);
		return (action == null ? null : action.getValue());
	}

	public ZLStringOption getOption(int keyId) {
		return myBindingsMap.get(keyId);
	}
}
