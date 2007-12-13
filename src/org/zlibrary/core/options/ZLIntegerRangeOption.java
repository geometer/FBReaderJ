package org.zlibrary.core.options;

/**
 * класс ранжированная целочисленная опция. есть верхний и нижний пределы,
 * которые тут же и указываются.
 * 
 * @author Администратор
 * 
 */
public final class ZLIntegerRangeOption extends ZLOption {
	private final int myMinValue;
	private final int myMaxValue;

	private final int myDefaultValue;
	private int myValue;

	public ZLIntegerRangeOption(String category, String group, String optionName, int minValue, int maxValue, int defaultValue) {
		super(category, group, optionName);
		myMinValue = minValue;
		myMaxValue = maxValue;
		myDefaultValue = Math.max(myMinValue, Math.min(myMaxValue, defaultValue));
		myValue = myDefaultValue;
	}

	public int getMinValue() {
		return myMinValue;
	}

	public int getMaxValue() {
		return myMaxValue;
	}

	public int getValue() {
		if (!myIsSynchronized) {
			String value = getConfigValue(null);
			if ((value != null) && (value.length() > 0)) {
				try {
					Integer intValue = Integer.parseInt(value);
					myValue = Math.max(myMinValue, Math.min(myMaxValue, intValue));
				} catch (NumberFormatException e) {
					// System.err.println(e);
				}
			}
			myIsSynchronized = true;
		}
		return myValue;
	}

	public void setValue(int value) {
		value = Math.max(myMinValue, Math.min(myMaxValue, value));
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		myIsSynchronized = true;
		if (myValue == myDefaultValue) {
			unsetConfigValue();
		} else {
			setConfigValue("" + myValue);
		}
	}
}
