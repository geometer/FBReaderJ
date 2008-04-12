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

package org.geometerplus.zlibrary.core.optionEntries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.application.ZLKeyBindings;
import org.geometerplus.zlibrary.core.dialogs.ZLKeyOptionEntry;
import org.geometerplus.zlibrary.core.util.*;

public abstract class ZLSimpleKeyOptionEntry extends ZLKeyOptionEntry {
	private ZLKeyBindings myBindings;
	private final HashMap/*<std::string,std::string>*/ myChangedCodes = new HashMap();

	public ZLSimpleKeyOptionEntry(ZLKeyBindings bindings) {
		super();
		myBindings = bindings;
	}

	public int actionIndex(String key) {
		String code = (String) myChangedCodes.get(key);
		return codeIndexBimap().indexByCode((code != null) ? code : myBindings.getBinding(key));
	}

	public void onAccept() {
		for (Iterator it = myChangedCodes.entrySet().iterator(); it.hasNext(); ) {
			Entry entry = (Entry) it.next();
			myBindings.bindKey((String) entry.getKey(), (String) entry.getValue());
		}
		myBindings.saveCustomBindings();
	}

	public void onReset() {
		myChangedCodes.clear();
	}

	public void onKeySelected(String key) {}

	public void onValueChanged(String key, int index) {
		myChangedCodes.put(key, codeIndexBimap().codeByIndex(index));
	}

	public abstract CodeIndexBimap codeIndexBimap();

	public static class CodeIndexBimap {
		private final ArrayList/*<String>*/ CodeByIndex = new ArrayList();
		private final HashMap/*<std::string,int>*/ IndexByCode = new HashMap();

		public void insert(final String code) {
			IndexByCode.put(code, CodeByIndex.size());
			CodeByIndex.add(code);
		}

		public int indexByCode(final String code) {
			return IndexByCode.get(code) == null ? 0 : (Integer) IndexByCode.get(code); 
		}

		public String codeByIndex(int index) {
			if ((index < 0) || (index >= (int) CodeByIndex.size())) {
				return ZLApplication.NoAction;
			}
			return (String) CodeByIndex.get(index);
		}
	}
}
