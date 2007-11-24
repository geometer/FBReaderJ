package org.zlibrary.core.options.util;

public class ZLToStringConverter {
	public static String convert(boolean input) {
		 return input ? "true" : "false";
	}
	
	public static String convert(ZLBoolean3 input) {
		return input.getStringValue();
	}

	public static String convert(double input) {
		Double value = input;
		return value.toString();
	}

	public static String convert(int input){
		Integer value = input;
		return value.toString();
	}
	
	public static String convert(long input){
		Long value = input;
		return value.toString();
	}
}
