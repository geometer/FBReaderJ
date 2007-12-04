package org.zlibrary.core.options;

/**
 * класс строковая опция.
 * 
 * @author Администратор
 */
public final class ZLStringOption extends ZLSimpleOption {
	private String myValue;

	private final String myDefaultValue;

	public ZLStringOption(String category, String group, String optionName,
			String defaultValue) {
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}

	public ZLOptionType getType() {
		return ZLOptionType.TYPE_STRING;
	}

	public String getValue() {
		if (!myIsSynchronized) {
			String value = myConfig.getValue(myGroup, myOptionName,
					myDefaultValue);
			if (value != null) {
				myValue = value;
			}
			myIsSynchronized = true;
		}
		return myValue;
	}

	public void setValue(String value) {
		if (myIsSynchronized && (myValue.equals(value))) {
			return;
		}
		myValue = value;
		myIsSynchronized = true;
		myConfig.setValue(myGroup, myOptionName, myValue, myCategory);
	}
}
