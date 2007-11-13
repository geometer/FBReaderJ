package org.zlibrary.options.util;

/**
 * сущность "значение трехзначной логики"
 * @author Администратор
 */
public enum ZLBoolean3 {
	B3_FALSE("FALSE"),
	B3_TRUE("TRUE"),
	B3_UNDEFINED("UNDEFINED");
    
    private String myStringValue;
    
    private ZLBoolean3(String stringValue){
        myStringValue = stringValue;
    }
    
    public String getStringValue(){
        return myStringValue;
    }
}
