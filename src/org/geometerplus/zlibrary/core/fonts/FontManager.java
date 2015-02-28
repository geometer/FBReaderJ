/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.core.fonts;

import java.util.*;

public class FontManager {
	private final ArrayList<List<String>> myFamilyLists = new ArrayList<List<String>>();
	public final Map<String,FontEntry> Entries =
		Collections.synchronizedMap(new HashMap<String,FontEntry>());

	public synchronized int index(List<String> families) {
		for (int i = 0; i < myFamilyLists.size(); ++i) {
			if (myFamilyLists.get(i).equals(families)) {
				return i;
			}
		}
		myFamilyLists.add(new ArrayList<String>(families));
		return myFamilyLists.size() - 1;
	}

	public synchronized List<FontEntry> getFamilyEntries(int index) {
		try {
			final List<String> families = myFamilyLists.get(index);
			final ArrayList<FontEntry> entries = new ArrayList<FontEntry>(families.size());
			for (String f : families) {
				final FontEntry e = Entries.get(f);
				entries.add(e != null ? e : FontEntry.systemEntry(f));
			}
			return entries;
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
}
