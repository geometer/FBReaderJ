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

package org.geometerplus.zlibrary.core.optionEntries;

import java.util.*;

import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;

public class ZLFontFamilyOptionEntry extends ZLComboOptionEntry {
	private final ZLStringOption myOption;
	private final ZLPaintContext myContext;
	
	public ZLFontFamilyOptionEntry(ZLStringOption option, ZLPaintContext context) {
		myOption = option;
		myContext = context;
		String value = option.getValue();
		if (value != null && (value.length() > 0)) {
			option.setValue(myContext.realFontFamilyName(value));
		}	
	}

	public ArrayList<String> getValues() {
		return myContext.fontFamilies();
	}

	public String initialValue() {
		final ArrayList<String> allValues = getValues();
		final String value = myOption.getValue();
		for (String v : getValues()) {
			if (value.equals(myContext.realFontFamilyName(v))) {
				return v;
			}
		}
		return value;
	}

	public void onAccept(String value) {
		if (value.length() == 0) {
			myOption.setValue(value);
		} else {
			myOption.setValue(myContext.realFontFamilyName(value));
		}
	}
}
