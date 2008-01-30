package org.fbreader.description;

import java.util.*;
import org.zlibrary.core.util.*;

import org.fbreader.description.BookDescription.BookInfo;
import org.fbreader.formats.FormatPlugin.PluginCollection;
import org.fbreader.option.FBOptions;
import org.zlibrary.core.filesystem.ZLDir;
import org.zlibrary.core.filesystem.ZLFile;
import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.options.ZLStringOption;

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
