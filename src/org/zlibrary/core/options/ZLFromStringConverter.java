package org.zlibrary.core.options;

import org.zlibrary.core.options.util.ZLBoolean3;
import org.zlibrary.core.options.util.ZLColor;

/*package*/ class ZLFromStringConverter {
	
	public static ZLBoolean3 getBoolean3Value(String input){
		if (input.toLowerCase().equals("true")) {
			return ZLBoolean3.B3_TRUE;
		}
		if (input.toLowerCase().equals("false")) {
			return ZLBoolean3.B3_FALSE;
		}
		return ZLBoolean3.B3_UNDEFINED;
	}
	
	public static boolean getBooleanValue(String input){
		return input.toLowerCase().equals("true");
	}

	public static ZLColor getColorValue(String input){
		return new ZLColor(getIntegerValue(input));
	}

	public static double getDoubleValue(String input){
		try {
			double value = Double.parseDouble(input);
			return value;
		} catch (NumberFormatException e) {
			System.err.println("wrong format : " + input);
			return 0;
		}
	}

	public static int getIntegerValue(String input){
		try {
			int value = Integer.parseInt(input);
			return value;
		} catch (NumberFormatException e) {
			//e.printStackTrace();
			System.err.println("wrong format : " + input);
			return 0;
		}
	}
}
