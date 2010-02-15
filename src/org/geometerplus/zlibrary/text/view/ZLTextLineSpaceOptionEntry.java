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

package org.geometerplus.zlibrary.text.view;

import java.util.ArrayList;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;


public class ZLTextLineSpaceOptionEntry extends ZLComboOptionEntry {
	private static final String KEY_UNCHANGED = "unchanged";
	private static final ArrayList ourAllValues = new ArrayList();
	private static final ArrayList ourAllValuesPlusBase = new ArrayList();

	private final ZLIntegerOption myOption;
	private final ZLResource myResource;
	private final boolean myAllowBase;
	
	public ZLTextLineSpaceOptionEntry(ZLIntegerOption option, final ZLResource resource, boolean allowBase) {
		myOption = option;
		myResource = resource;
		myAllowBase = allowBase;
		if (ourAllValuesPlusBase.size() == 0) {
			for (int i = 5; i <= 20; ++i) {
				ourAllValues.add("" + (char)(i / 10 + '0') + '.' + (char)(i % 10 + '0'));
			}
			ourAllValuesPlusBase.add(myResource.getResource(KEY_UNCHANGED).getValue());
			ourAllValuesPlusBase.addAll(ourAllValues);
		}
	}
		
	public ArrayList getValues() {
		return myAllowBase ? ourAllValuesPlusBase : ourAllValues;
	}

	public String initialValue() {
		final int value = myOption.getValue();
		if (value == -1) {
			return (String) ourAllValuesPlusBase.get(0);
		}
		final int index = Math.max(0, Math.min(15, (value + 5) / 10 - 5));
		return (String) ourAllValues.get(index);
	}

	public void onAccept(String value) {
		if (ourAllValuesPlusBase.get(0).equals(value)) {
			myOption.setValue(-1);
		} else {
			for (int i = 5; i <= 20; ++i) {
				if (ourAllValues.get(i - 5).equals(value)) {
					myOption.setValue(10 * i);
					break;
				}
			}
		}
	}
}
