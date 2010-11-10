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

package org.geometerplus.fbreader.optionsDialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.geometerplus.zlibrary.core.dialogs.ZLOptionEntry;

final class ComboOptionEntry extends ZLComboOptionEntry {
	private final OptionsPage myPage;
	private final ArrayList<String> myValues = new ArrayList<String>();
	
	public ComboOptionEntry(final OptionsPage myPage) {
		this.myPage = myPage;
	}

	public ArrayList<String> getValues() {
		return myValues;
	}

	public String initialValue() {
		return myValues.get(0);
	}

	public void onAccept(String value) {}

	@Override
	public void onReset() {
		onValueSelected(0);
	}

	@Override
	public void onValueSelected(int index) {
		final Object selectedValue = myValues.get(index);
		final LinkedHashMap<ZLOptionEntry, String> entries = myPage.getEntries();
/*		for (Iterator it = entries.keySet().iterator(); it.hasNext(); ) {
			ZLOptionEntry entry = (ZLOptionEntry) it.next();
			entry.setVisible(selectedValue != null && selectedValue.equals(entries.get(entry)));
			if (entry.isVisible())
				System.out.println(entry.getKind()+" "+entry.hashCode());
		} 
	*/
		for (Iterator<Entry<ZLOptionEntry, String>> it = entries.entrySet().iterator(); it.hasNext(); ) {
			Entry<ZLOptionEntry, String> entry = it.next();
			entry.getKey().setVisible(selectedValue != null && selectedValue.equals(entry.getValue()));
		} 
	}

	public void addValue(final String value) {
		myValues.add(value);
	}
}
