package org.zlibrary.options;

import org.zlibrary.options.util.ZLFromStringConverter;
import org.zlibrary.options.util.ZLToStringConverter;

/**
 * класс дробная опция.
 * @author Администратор
 *
 */
public final class ZLDoubleOption extends ZLOption{
	private double myValue;
	private final double myDefaultValue;
	
	public ZLDoubleOption (String category, String group, String optionName, double defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}
	
	public double getValue(){
		if (!myIsSynchronized){
			String strDefaultValue = ZLToStringConverter.convert(myDefaultValue);
			String value = myConfig.getValue(myCategory, myGroup, myOptionName, strDefaultValue);
			myValue = ZLFromStringConverter.getDoubleValue(value);
			myIsSynchronized = true;
		}
		return myValue;
	}
	
	public void setValue(double value) {
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		myIsSynchronized = true;
		if (myValue == myDefaultValue) {
			myConfig.unsetValue(myCategory, myGroup, myOptionName);
		} else {
			String stringValue = ZLToStringConverter.convert(myValue);
			myConfig.setValue(myCategory, myGroup, myOptionName, stringValue);
		}
	}
}
