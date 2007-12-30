package org.zlibrary.core.options;

public final class ZLBooleanOption extends ZLOption implements ZLSimpleOption {
	private final boolean myDefaultValue;
	private boolean myValue;

	public ZLBooleanOption(String category, String group, String optionName, boolean defaultValue) {
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}

	public int getType() {
		return Type.BOOLEAN;
	}

	public boolean getValue() {
		if (!myIsSynchronized) {
			String value = getConfigValue(null);
			if (value != null) {
				if ("true".equals(value)) {
					myValue = true;
				} else if ("false".equals(value)) {
					myValue = false;
				}
			}
			myIsSynchronized = true;
		}
		return myValue;
	}

	public void setValue(boolean value) {
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		myIsSynchronized = true;
		if (myValue == myDefaultValue) {
			unsetConfigValue();
			// System.out.println("unsett" + myValue);
		} else {
			setConfigValue(myValue ? "true" : "false");
		}
	}
}
