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

import java.util.*;

import org.geometerplus.zlibrary.core.dialogs.*;

class OptionsPage {
	private final LinkedHashMap<ZLOptionEntry, String> myEntries = new LinkedHashMap<ZLOptionEntry, String>();
	protected ComboOptionEntry myComboEntry;

	protected OptionsPage() {
	}

	protected void registerEntry(ZLDialogContent tab, final String entryKey, ZLOptionEntry entry, final String name) {
		if (entry != null) {
			entry.setVisible(false);
			myEntries.put(entry, name);
		}
		tab.addOption(entryKey, entry);
	}
	
	protected void registerEntries(ZLDialogContent tab, final String entry0Key,
		ZLOptionEntry entry0, final String entry1Key, ZLOptionEntry entry1, final String name) {
		if (entry0 != null) {
			entry0.setVisible(false);
			myEntries.put(entry0, name);
		}
		if (entry1 != null) {
			entry1.setVisible(false);
			myEntries.put(entry1, name);
		}
		tab.addOptions(entry0Key, entry0, entry1Key, entry1);
	}

	LinkedHashMap<ZLOptionEntry, String> getEntries() {
		return myEntries;
	}
}
