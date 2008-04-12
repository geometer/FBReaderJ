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

package org.geometerplus.fbreader.fbreader;

import java.util.ArrayList;

import org.geometerplus.fbreader.option.FBOptions;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;

class SearchPatternEntry extends ZLComboOptionEntry {
	SearchAction myAction;
	ArrayList /*String*/ myValues = new ArrayList();
	
	public SearchPatternEntry(SearchAction action) {
		super(true);
		myAction = action;
	}
	
	public void onAccept(final String value) {
		String v = value;
//		ZLStringUtil.stripWhiteSpaces(v);
		if (v != "" && v != (String) getValues().get(0)) {
			myAction.SearchPatternOption.setValue(v);
			int index = 1;
			for (int i = 0; (index < 6) && (i < myValues.size()); i++) {
				if (!myValues.get(i).equals(v)) {
					(new ZLStringOption(FBOptions.SEARCH_CATEGORY, SearchAction.SEARCH, SearchAction.PATTERN + index, "")).
						setValue((String) myValues.get(i));
					index++;
				}
			}
		}
	}

	public ArrayList getValues() {
		if (myValues.isEmpty()) {
			myValues.add(myAction.SearchPatternOption.getValue());	
			for (int i = 1; i < 6; i++) {
				String value = (new ZLStringOption(FBOptions.SEARCH_CATEGORY, 
					SearchAction.SEARCH, SearchAction.PATTERN + i, "")).getValue();
				if (value != "") {
					myValues.add(value);
				}
			}
		}
		return myValues;
	}

	public String initialValue() {
		return (String) getValues().get(0);
	}
}
