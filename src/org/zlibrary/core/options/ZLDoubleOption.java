package org.zlibrary.core.options;

/**
 * класс дробная опция.
 * @author Администратор
 *
 */
public final class ZLDoubleOption extends ZLOption{
	private double myValue;
	private final double myDefaultValue;
	
	public ZLDoubleOption (String category, String group, String optionName, double defaultValue) {
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}
	
	public double getValue() {
		if (!myIsSynchronized) {
			String strDefaultValue = ((Double)myDefaultValue).toString();
			String value = myConfig.getValue(myGroup, myOptionName, strDefaultValue);
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
			myConfig.unsetValue(myGroup, myOptionName);
		} else {
			String stringValue = ((Double)myValue).toString();
			myConfig.setValue(myGroup, myOptionName, stringValue, myCategory);
		}
	}
}
