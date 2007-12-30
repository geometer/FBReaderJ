package org.zlibrary.core.options;

import org.zlibrary.core.util.ZLBoolean3;

public final class ZLBoolean3Option extends ZLOption implements ZLSimpleOption {
	private int myValue;
	private final int myDefaultValue;

	public ZLBoolean3Option(String category, String group, String optionName, int defaultValue) {
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}

	public int getType() {
		return Type.BOOLEAN3;
	}

	public int getValue() {
		if (!myIsSynchronized) {
			String value = getConfigValue(null);
			if (value != null) {
				myValue = ZLBoolean3.getByString(value);
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
			unsetConfigValue();
		} else {
			setConfigValue(ZLBoolean3.getName(myValue));
		}
	}
}
