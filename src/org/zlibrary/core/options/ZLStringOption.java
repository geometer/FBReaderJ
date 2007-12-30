package org.zlibrary.core.options;

/**
 * класс строковая опция.
 * 
 * @author Администратор
 */
public final class ZLStringOption extends ZLOption implements ZLSimpleOption {
	private final String myDefaultValue;
	private String myValue;

	public ZLStringOption(String category, String group, String optionName, String defaultValue) {
		super(category, group, optionName);
		myDefaultValue = (defaultValue != null) ? defaultValue.intern() : "";
		myValue = myDefaultValue;
	}

	public void changeName(String optionName) {
		super.changeName(optionName);
		myValue = myDefaultValue;
	}

	public int getType() {
		return Type.STRING;
	}

	public String getValue() {
		if (!myIsSynchronized) {
			String value = getConfigValue(myDefaultValue);
			if (value != null) {
				myValue = value;
			}
			myIsSynchronized = true;
		}
		return myValue;
	}

	public void setValue(String value) {
		if (value == null) {
			return;
		}
		value = value.intern();
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		if (value == myDefaultValue) {
			unsetConfigValue();
		} else {
			setConfigValue(value);
		}
		myIsSynchronized = true;
	}
}
