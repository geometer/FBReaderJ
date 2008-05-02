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

package org.geometerplus.fbreader.collection;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.options.*;

public class BookList {
	private final static String GROUP = "BookList";
	private static final String BOOK = "Book";
	private static final String SIZE = "Size";

	private final ArrayList myFileNames = new ArrayList();
	
	public BookList() {
		final int len = new ZLIntegerOption(ZLOption.STATE_CATEGORY, GROUP, SIZE, 0).getValue();
		final ArrayList fileNames = myFileNames;
		final ZLStringOption bookOption = new ZLStringOption(ZLOption.STATE_CATEGORY, GROUP, "", "");
		for (int i = 0; i < len; ++i) {
			bookOption.changeName(BOOK + i);
			final String name = bookOption.getValue();
			if ((name.length() != 0) && !fileNames.contains(name)) {
				fileNames.add(name);
			}
		}
	}

	public ArrayList fileNames() {
		return new ArrayList(myFileNames);
	}
	
	public void addFileName(String fileName) {
		if (!myFileNames.contains(fileName)) {
			myFileNames.add(fileName);
			save();
		}
	}
	
	public void removeFileName(String fileName) {
		myFileNames.remove(fileName);
		save();
	}
	
	private void save() {
		final ArrayList fileNames = myFileNames;
		final int len = fileNames.size();
		new ZLIntegerOption(ZLOption.STATE_CATEGORY, GROUP, SIZE, 0).setValue(len);
		final ZLStringOption bookOption =
			new ZLStringOption(ZLOption.STATE_CATEGORY, GROUP, "", "");
		for (int i = 0; i < len; ++i) {	
			bookOption.changeName(BOOK + i);
			bookOption.setValue((String)fileNames.get(i));
		}
	}
}
