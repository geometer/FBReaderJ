package org.zlibrary.options.util;

public class ZLFromStringConverter {
	public static ZLBoolean3 getBoolean3Value(String input){
		if (input.equals("true")) {
			return ZLBoolean3.B3_TRUE;
		} else if (input.equals("false")) {
			return ZLBoolean3.B3_FALSE;
		} else {
			return ZLBoolean3.B3_UNDEFINED;
		}
	}
	
	public static boolean getBooleanValue(String input){
		return input.equals("true");
	}

	public static long getLongValue(String input){
		Long value = Long.parseLong(input);
		return value;
	}
	
	public static ZLColor getColorValue(String input){
		return new ZLColor((int)getLongValue(input));
	}

	public static double getDoubleValue(String input){
		double value = Double.parseDouble(input);
		return value;
	}

	public static int getIntegerValue(String input){
		int value = Integer.parseInt(input);
		return value;
	}
}
