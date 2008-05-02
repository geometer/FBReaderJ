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

package org.geometerplus.fbreader.encodingOption;

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;

public class EncodingSetEntry extends ZLComboOptionEntry {
	private EncodingEntry myEncodingEntry;
	
	public EncodingSetEntry(EncodingEntry encodingEntry) {
		myEncodingEntry = encodingEntry;
	}

	public ArrayList getValues() {
		return myEncodingEntry.mySetNames;
	}

	public String initialValue() {
		return myEncodingEntry.myInitialSetName;
	}

	public void onAccept(String value) {}

	public void onValueSelected(int index) {
		myEncodingEntry.myInitialSetName = (String) getValues().get(index);
		myEncodingEntry.resetView();
	}
}
