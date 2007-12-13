package org.zlibrary.core.options;

/**
 * класс дробная опция.
 * 
 * @author Администратор
 * 
 */
public final class ZLDoubleOption extends ZLOption {
	private final double myDefaultValue;
	private double myValue;

	public ZLDoubleOption(String category, String group, String optionName, double defaultValue) {
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}

	public double getValue() {
		if (!myIsSynchronized) {
			String value = getConfigValue(null);
			if (value != null) {
				try {
					Double doubleValue = Double.parseDouble(value);
					myValue = doubleValue;
				} catch (NumberFormatException e) {
					// System.err.println(e);
				}
			}
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
			unsetConfigValue();
		} else if (Double.isNaN(myValue) && Double.isNaN(myDefaultValue)) {
			unsetConfigValue();
		} else {
			setConfigValue("" + myValue);
		}
	}
}
