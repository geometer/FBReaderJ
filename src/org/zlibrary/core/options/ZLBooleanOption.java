package org.zlibrary.core.options;

public final class ZLBooleanOption extends ZLSimpleOption {

	private boolean myValue;

	private final boolean myDefaultValue;

	public ZLBooleanOption(String category, String group, String optionName,
			boolean defaultValue) {
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}

	public ZLOptionType getType() {
		return ZLOptionType.TYPE_BOOLEAN;
	}

	public boolean getValue() {
		if (!myIsSynchronized) {

			String value = myConfig.getValue(myGroup, myOptionName, null);
			if (value != null) {
				if (value.toLowerCase().equals("true")) {
					myValue = true;
					myConfig.setValue(myGroup, myOptionName, value, myCategory);
				} else if (value.toLowerCase().equals("false")) {
					myValue = false;
					//myConfig.setValue(myGroup, myOptionName, value, myCategory);
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
			myConfig.unsetValue(myGroup, myOptionName);
			// System.out.println("unsett" + myValue);

		} else {
			if (myValue) {
				myConfig.setValue(myGroup, myOptionName, "true", myCategory);
			} else {
				myConfig.setValue(myGroup, myOptionName, "false", myCategory);
			}
		}
	}
}
