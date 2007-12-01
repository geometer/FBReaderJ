package org.zlibrary.core.options;

/**
 * класс целочисленная опция.
 * @author Администратор
 *
 */
public final class ZLIntegerOption extends ZLOption{
	private int myValue;
	private final int myDefaultValue;
	
	public ZLIntegerOption (String category, String group, String optionName, int defaultValue) {
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}
	
	public int getValue() {
		if (!myIsSynchronized) {
			String value = myConfig.getValue(myGroup, myOptionName, null);
			if (value != null) {
				myValue = ZLFromStringConverter.getIntegerValue(value);
			}
			myIsSynchronized = true;
		}
		return myValue;
	}
	
	public void setValue(int value) {
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		myIsSynchronized = true;
		if (myValue == myDefaultValue) {
			myConfig.unsetValue(myGroup, myOptionName);
		} else {
			String stringValue = ((Integer)myValue).toString();
			myConfig.setValue(myGroup, myOptionName, stringValue, myCategory);
		}
	}
}
