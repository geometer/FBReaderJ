package org.zlibrary.options.util;

public class ZLToStringConverter {

    public static String convert(boolean input) {
       if (input) {
           return "TRUE";
       } else {
           return "FALSE";
       }
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
