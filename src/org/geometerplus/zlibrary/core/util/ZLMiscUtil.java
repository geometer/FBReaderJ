package org.geometerplus.zlibrary.core.util;

public abstract class ZLMiscUtil {
	public static boolean equals(Object o0, Object o1) {
		return (o0 == null) ? (o1 == null) : o0.equals(o1);
	}

	public static boolean matchesIgnoreCase(String text, String lowerCasePattern) {
		return (text.length() >= lowerCasePattern.length()) &&
			   (text.toLowerCase().indexOf(lowerCasePattern) >= 0);
	}
}
