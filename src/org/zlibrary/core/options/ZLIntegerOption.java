package org.zlibrary.core.options;

/**
 * класс целочисленная опция.
 * 
 * @author Администратор
 * 
 */
public final class ZLIntegerOption extends ZLOption {
	private final int myDefaultValue;
	private int myValue;

	public ZLIntegerOption(String category, String group, String optionName,
			int defaultValue) {
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}

	public void changeName(String optionName) {
		super.changeName(optionName);
		myValue = myDefaultValue;
	}

	public int getValue() {
		if (!myIsSynchronized) {
			String value = getConfigValue(null);
			if (value != null) {
				try {
					Integer intValue = Integer.parseInt(value);
					myValue = intValue;
				} catch (NumberFormatException e) {
					// System.err.println(e);
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
			// System.out.println(optionName() + " is unset");
			unsetConfigValue();
		} else {
			setConfigValue("" + myValue);
		}
	}
}
