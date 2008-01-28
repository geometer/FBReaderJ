package org.zlibrary.core.util;

public final class Locale {
	static private Locale ourDefaultLocale;

	private final String myLanguage;

	private Locale(String property) {
		if (property == null) {
			final int index = property.indexOf('-');
			myLanguage = (index != -1) ? property.substring(0, index) : property;
		} else {
			myLanguage = "en";
		}
	}

	public static Locale getDefault() {
		if (ourDefaultLocale == null) {
			ourDefaultLocale = new Locale(System.getProperty("microedition.locale"));
		}
		return ourDefaultLocale;
	}

	public String getLanguage() {
		return myLanguage;
	}
}
