package org.fbreader.description;

import java.util.List;

import org.fbreader.option.FBOptions;
import org.zlibrary.core.filesystem.ZLDir;
import org.zlibrary.core.filesystem.ZLFile;
import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.options.ZLStringOption;

class BookDescriptionUtil {
	private static final String SIZE = "Size";
	private static final String ENTRY = "Entry";
	private static final String ENTRIES_NUMBER = "EntriesNumber";

	
	public static boolean checkInfo(ZLFile file) {
		ZLIntegerOption op = new ZLIntegerOption(FBOptions.BOOKS_CATEGORY, file.path(), SIZE, -1);
		return op.getValue() == (int)file.size();

	}
	
	public static void saveInfo(ZLFile file) {
		new ZLIntegerOption(FBOptions.BOOKS_CATEGORY, file.path(), SIZE, -1).setValue((int)file.size());		
	}
	
	public static void listZipEntries(ZLFile zipFile, List<String> entries) {
		int entriesNumber = new ZLIntegerOption(FBOptions.BOOKS_CATEGORY, zipFile.path(), ENTRIES_NUMBER, -1).getValue();
		if (entriesNumber == -1) {
			//??? why so??resetZipInfo(zipFile.path());
			resetZipInfo(zipFile);
			entriesNumber = new ZLIntegerOption(FBOptions.BOOKS_CATEGORY, zipFile.path(), ENTRIES_NUMBER, -1).getValue();
		}
		for (int i = 0; i < entriesNumber; ++i) {
			String optionName = ENTRY;
			optionName = optionName + i;
			//ZLStringUtil.appendNumber(optionName, i);
			String entry = new ZLStringOption(FBOptions.BOOKS_CATEGORY, zipFile.path(), optionName, "").getValue();
			if (!entry.equals("")) {
				entries.add(entry);
			}
		}

	}
	
	public static void resetZipInfo(ZLFile zipFile) {
		//ZLOption.clearGroup(zipFile.path());

		ZLDir zipDir = zipFile.directory(false);
		/*if (!zipDir.isNull()) {
			String zipPrefix = zipFile.path() + ':';
			List<String> entries;
			int counter = 0;
			zipDir.collectFiles(entries, false);
			for (List<String>iterator zit = entries.begin(); zit != entries.end(); ++zit) {
				if (PluginCollection.instance().plugin(ZLFile(zit), true) != 0) {
					String optionName = ENTRY;
					//ZLStringUtil::appendNumber(optionName, counter);
					optionName += counter;
					String fullName = zipPrefix + zit;
					new ZLStringOption(FBOptions.BOOKS_CATEGORY, zipFile.path(), optionName, "").setValue(fullName);
					new BookInfo(fullName).reset();
					++counter;
				}
			}
			new ZLIntegerOption(FBOptions.BOOKS_CATEGORY, zipFile.path(), ENTRIES_NUMBER, -1).setValue(counter);
		}*/
	}
}
