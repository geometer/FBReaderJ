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

import java.util.*;

import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.geometerplus.zlibrary.core.language.ZLLanguageList;
import org.geometerplus.zlibrary.core.options.*;

public class ZLLanguageOptionEntry extends ZLComboOptionEntry {
	private final ArrayList myValues = new ArrayList();
	private	final TreeMap myValuesToCodes = new TreeMap();
	private	String myInitialValue;
	private	ZLStringOption myLanguageOption;

	public ZLLanguageOptionEntry(ZLStringOption languageOption, ArrayList languageCodes) {
		myLanguageOption = languageOption;
		final String initialCode = myLanguageOption.getValue();
		final int len = languageCodes.size();
		for (int i = 0; i < len; ++i) {
			final String code = (String)languageCodes.get(i);
			final String name = ZLLanguageList.languageName(code);
			myValuesToCodes.put(name, code);
			if (initialCode.equals(code)) {
				myInitialValue = name;
			}
		}
		for (Iterator it = myValuesToCodes.keySet().iterator(); it.hasNext(); ) {
			myValues.add(it.next());
		}
		final String otherCode = "other";
		final String otherName = ZLLanguageList.languageName(otherCode);
		myValues.add(otherName);
		myValuesToCodes.put(otherName,otherCode);
		if ((myInitialValue == null) || (myInitialValue.length() == 0)) {
			myInitialValue = otherName;
		}
	}

	public String initialValue() {
		return myInitialValue;
	}
	
	public ArrayList getValues() {
		return myValues;
	}
	
	public void onAccept(String value) {
		myLanguageOption.setValue((String)myValuesToCodes.get(value));
	}
}
