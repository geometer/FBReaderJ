package org.zlibrary.core.options.util;

/**
 * сущность "значение трехзначной логики"
 * @author Администратор
 */
public enum ZLBoolean3 {
	B3_FALSE("false"),
	B3_TRUE("true"),
	B3_UNDEFINED("undefined");
	
	private String myStringValue;
	
	private ZLBoolean3(String stringValue){
		myStringValue = stringValue;
	}

	public static ZLBoolean3 getByString(String name) {
		if (B3_TRUE.myStringValue.equals(name)) {
			return B3_TRUE;
		}
		if (B3_FALSE.myStringValue.equals(name)) {
			return B3_FALSE;
		}
		return B3_UNDEFINED;
	}
	
	public String toString(){
		return myStringValue;
	}
}
