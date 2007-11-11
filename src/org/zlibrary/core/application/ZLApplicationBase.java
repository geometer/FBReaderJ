package org.zlibrary.core.application;

import org.zlibrary.core.library.ZLibrary;

public class ZLApplicationBase {
	public static String BaseDirectory;//= BASEDIR;
	public static String HomeDirectory;//=HOMEDIR

	private static String ourImageDirectory;
	private static String ourApplicationImageDirectory;
	private static String ourApplicationName;
	private static String ourZLibraryDirectory;
	private static String ourApplicationDirectory;
	private static String ourDefaultFilesPathPrefix;

	protected ZLApplicationBase(String name) {
		ourApplicationName = name;
		//ourImageDirectory = replaceRegExps(IMAGEDIR);
		//ourApplicationImageDirectory = replaceRegExps(APPIMAGEDIR);
		//ourZLibraryDirectory = BaseDirectory + ZLibrary.FileNameDelimiter + "zlibrary";
		//ourApplicationDirectory = BaseDirectory + ZLibrary.FileNameDelimiter + ourApplicationName;
		//ourDefaultFilesPathPrefix = ourApplicationDirectory + ZLibrary.FileNameDelimiter + "default" + ZLibrary.FileNameDelimiter;
		//ZLOptions.createInstance();
	}

	
	public static String getApplicationName() {
		return ourApplicationName;
	}
	
	public static String getImageDirectory() {
		return ourImageDirectory;
	}
	
	public static String getApplicationImageDirectory() {
		return ourApplicationImageDirectory;
	}

	public static String getZLibraryDirectory() {
		return ourZLibraryDirectory;
	}
	
	public static String ApplicationDirectory() {
		return ourApplicationDirectory;
	}
	
	public static String DefaultFilesPathPrefix() {
		return ourDefaultFilesPathPrefix;
	}

	private String replaceRegExps(String pattern) {
		final String NAME_PATTERN = "%APPLICATION_NAME%";
		final String LOWERCASENAME_PATTERN = "%application_name%";
		String str = pattern;
		int index = -1;
		while ((index = str.indexOf(NAME_PATTERN)) != -1) {
		   // str.erase(index, NAME_PATTERN.length());
		   // str.insert(index, ourApplicationName);
		}
		while ((index = str.indexOf(LOWERCASENAME_PATTERN)) != -1) {
		    //str.erase(index, LOWERCASENAME_PATTERN.length());
			//str.insert(index, ZLUnicodeUtil::toLower(ourApplicationName));
		}
		return str;

	}
}

