package org.zlibrary.core.util;

public final class Locale {
	static public final Locale ENGLISH = new Locale("en");

	private final String myLanguage;

	public Locale(String language) {
		myLanguage = language;
	}

	public static Locale getDefault() {
		return ENGLISH;
	}

	public String getLanguage() {
		return myLanguage;
	}
}
