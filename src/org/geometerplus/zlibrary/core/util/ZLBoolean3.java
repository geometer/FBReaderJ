package org.geometerplus.zlibrary.core.util;

public final class ZLBoolean3 {
	public static final int B3_FALSE = 0;
	public static final int B3_TRUE = 1;
	public static final int B3_UNDEFINED = 2;
	
	private static final String STRING_FALSE = "false";
	private static final String STRING_TRUE = "true";
	private static final String STRING_UNDEFINED = "undefined";

	public static int getByString(String name) {
		if (STRING_TRUE.equals(name)) {
			return B3_TRUE;
		}
		if (STRING_FALSE.equals(name)) {
			return B3_FALSE;
		}
		return B3_UNDEFINED;
	}
	
	public static String getName(int value) {
		switch (value) {
			case B3_FALSE:
				return STRING_FALSE;
			case B3_TRUE:
				return STRING_TRUE;
			default:
				return STRING_UNDEFINED;
		}
	}

	private ZLBoolean3() {
	}
}
