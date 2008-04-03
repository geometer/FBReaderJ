package org.zlibrary.core.language;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

import org.zlibrary.core.filesystem.ZLDir;
import org.zlibrary.core.filesystem.ZLFile;
import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.resources.ZLResource;

public class ZLLanguageList {
	private static ArrayList ourLanguageCodes = new ArrayList();
	
	public static ArrayList languageCodes() {
		if (ourLanguageCodes.isEmpty()) {
			TreeSet codes = new TreeSet();
			codes.add("zh");
			ZLDir dir = patternsDirectory();
			if (dir != null) {
				final ArrayList fileNames = dir.collectFiles();
				final int len = fileNames.size();
				for (int i = 0; i < len; ++i) {
					String name = (String)fileNames.get(i);
					final int index = name.indexOf("_");
					if (index != -1) {
						String str = name.substring(0, index);
						if (!codes.contains(str)) {
						    codes.add(str);
						}
					}
				}
			}

			ourLanguageCodes.addAll(codes);
		}

		return ourLanguageCodes;
	}
	
	public	static String languageName(String code) {
		return ZLResource.resource("language").getResource(code).getValue();
	}

	public	static ZLDir patternsDirectory() {
		String dirName = ZLibrary.JAR_DATA_PREFIX + "data/languagePatterns.tar";
		return new ZLFile(dirName).getDirectory();
	}

	private ZLLanguageList() {}
}
