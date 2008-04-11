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
package org.geometerplus.fbreader.description;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.fbreader.description.BookDescription.BookInfo;
import org.geometerplus.fbreader.formats.FormatPlugin.PluginCollection;
import org.geometerplus.fbreader.option.FBOptions;
import org.geometerplus.zlibrary.core.filesystem.ZLDir;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

public class BookDescriptionUtil {
	private static final String SIZE = "Size";
	private static final String ENTRY = "Entry";
	private static final String ENTRIES_NUMBER = "EntriesNumber";

	
	public static boolean checkInfo(ZLFile file) {
		ZLIntegerOption op = new ZLIntegerOption(FBOptions.BOOKS_CATEGORY, file.getPath(), SIZE, -1);
		return op.getValue() == (int)file.size();

	}
	
	public static void saveInfo(ZLFile file) {
		new ZLIntegerOption(FBOptions.BOOKS_CATEGORY, file.getPath(), SIZE, -1).setValue((int)file.size());		
	}
	
	public static void listZipEntries(ZLFile zipFile, ArrayList entries) {
		int entriesNumber = new ZLIntegerOption(FBOptions.BOOKS_CATEGORY, zipFile.getPath(), ENTRIES_NUMBER, -1).getValue();
		if (entriesNumber == -1) {
			//??? why so??resetZipInfo(zipFile.path());
			resetZipInfo(zipFile);
			entriesNumber = new ZLIntegerOption(FBOptions.BOOKS_CATEGORY, zipFile.getPath(), ENTRIES_NUMBER, -1).getValue();
		}
		for (int i = 0; i < entriesNumber; ++i) {
			String optionName = ENTRY;
			optionName += i;
			String entry = new ZLStringOption(FBOptions.BOOKS_CATEGORY, zipFile.getPath(), optionName, "").getValue();
			if (entry.length() != 0) {
				entries.add(entry);
			}
		}

	}
	
	public static void resetZipInfo(ZLFile zipFile) {
		//ZLOption.clearGroup(zipFile.path());

		ZLDir zipDir = zipFile.getDirectory();
		if (zipDir != null) {
			String zipPrefix = zipFile.getPath() + ':';
			int counter = 0;
			final ArrayList entries = zipDir.collectFiles();
			final int size = entries.size();
			for (int i = 0; i < size; ++i) { 
				String entry = (String)entries.get(i);
				if (PluginCollection.instance().getPlugin(new ZLFile(entry), true) != null) {
					String optionName = ENTRY;
					optionName += counter;
					String fullName = zipPrefix + entry;
					new ZLStringOption(FBOptions.BOOKS_CATEGORY, zipFile.getPath(), optionName, "").setValue(fullName);
					new BookInfo(fullName).reset();
					++counter;
				}
			}
			new ZLIntegerOption(FBOptions.BOOKS_CATEGORY, zipFile.getPath(), ENTRIES_NUMBER, -1).setValue(counter);
		}
	}
	
}
