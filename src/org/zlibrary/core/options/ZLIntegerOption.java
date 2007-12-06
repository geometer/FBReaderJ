package org.zlibrary.core.options;

/**
 * класс целочисленная опция.
 * @author Администратор
 *
 */
public final class ZLIntegerOption extends ZLOption {
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
			System.out.println(value);
			if (value != null) {
				try {
					Integer intValue = Integer.parseInt(value);
					myValue = intValue;
				} catch (NumberFormatException e) {
					//System.err.println(e);
				}
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
			//System.out.println(myOptionName + " is unset");
			myConfig.unsetValue(myGroup, myOptionName);
		} else {
			myConfig.setValue(myGroup, myOptionName, "" + myValue, myCategory);
		}
	}
}
