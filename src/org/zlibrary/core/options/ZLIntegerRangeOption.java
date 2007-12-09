package org.zlibrary.core.options;

/**
 * класс ранжированная целочисленная опция. есть верхний и нижний пределы,
 * которые тут же и указываются.
 * 
 * @author Администратор
 * 
 */
public final class ZLIntegerRangeOption extends ZLOption {
	private int myValue;

	private final int myDefaultValue;

	private final int myMinValue;

	private final int myMaxValue;

	public ZLIntegerRangeOption(String category, String group,
			String optionName, int minValue, int maxValue, int defaultValue) {
		super(category, group, optionName);
		myMinValue = minValue;
		myMaxValue = maxValue;
		myDefaultValue = Math.max(myMinValue, Math
				.min(myMaxValue, defaultValue));
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
			String value = myConfig.getValue(myGroup, myOptionName, null);
			if (value != null) {
				try {
					Integer intValue = Integer.parseInt(value);
					myValue = intValue;
					myConfig.setValue(myGroup, myOptionName, value , myCategory);
				} catch (NumberFormatException e) {
					// System.err.println(e);
				}
			}
			myValue = Math.max(myMinValue, Math.min(myMaxValue, myValue));
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
			myConfig.unsetValue(myGroup, myOptionName);
		} else {
			myConfig.setValue(myGroup, myOptionName, "" + myValue,
					myCategory);
		}
	}
}
