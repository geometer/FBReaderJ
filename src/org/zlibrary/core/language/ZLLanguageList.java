package org.zlibrary.core.language;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.zlibrary.core.filesystem.ZLDir;
import org.zlibrary.core.filesystem.ZLFile;
import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.resources.ZLResource;

public class ZLLanguageList {
	private static ArrayList/*<String>*/ ourLanguageCodes = new ArrayList();
	
	public static ArrayList/*<String>*/ languageCodes() {
		if (ourLanguageCodes.isEmpty()) {
			ArrayList/*<String>*/ codes = new ArrayList();
			codes.add("zh");
			ZLDir dir = patternsDirectory();
			if (dir != null) {
				final ArrayList fileNames = dir.collectFiles();
				if (fileNames != null) {
					final int len = fileNames.size();
					for (int i = 0; i < len; ++i) {
						String itstr = (String)fileNames.get(i);
						final int index = itstr.indexOf("_");
						if (index != -1) {
							String str = itstr.substring(0, index);
							if (!codes.contains(str)) {
							    codes.add(str);
							}
						}
					}
				}
			}

			for (Iterator it = codes.iterator(); it.hasNext(); ) {
				ourLanguageCodes.add(it.next());
			}
		}

		return ourLanguageCodes;
	}
	
	public	static String languageName(String code) {
		return ZLResource.resource("language").getResource(code).getValue();
	}

	public	static ZLDir patternsDirectory() {
		String dirName = ZLibrary.JAR_DATA_PREFIX + "zlibrary" + File.separator + "languagePatterns.zip";
		return new ZLFile(dirName).getDirectory(false);
	}

	private ZLLanguageList() {}
}
