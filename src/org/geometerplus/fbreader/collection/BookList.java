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

import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.options.ZLOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

public class BookList {
	private final static String GROUP = "BookList";
	static final String BOOK = "Book";
	static final String SIZE = "Size";
	private final ArrayList/*String*/ myFileNames = new ArrayList();
	
	public BookList() {
		int size = new ZLIntegerOption(ZLOption.STATE_CATEGORY, GROUP, SIZE, 0).getValue();
		for (int i = 0; i < size; ++i) {
			String optionName = BOOK;
			optionName += i;
			ZLStringOption bookOption = new ZLStringOption(ZLOption.STATE_CATEGORY, GROUP, optionName, "");
			final String fileName = bookOption.getValue();
			if ((fileName.length() != 0) && !myFileNames.contains(fileName)) {
				myFileNames.add(fileName);
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
	
	private void  save() {
		new ZLIntegerOption(ZLOption.STATE_CATEGORY, GROUP, SIZE, 0).setValue(myFileNames.size());
		int i = 0;
		for (Iterator it = myFileNames.iterator(); it.hasNext(); ++i) {
			String optionName = BOOK;
			optionName += i;
			new ZLStringOption(ZLOption.STATE_CATEGORY, GROUP, optionName, "").setValue((String)it.next());
		}
	}
}

