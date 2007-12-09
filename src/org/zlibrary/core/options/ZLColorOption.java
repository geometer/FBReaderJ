package org.zlibrary.core.options;

import org.zlibrary.core.options.util.*;

/**
 * класс опция цвета. внутри опции цвет хранится одним числом, чтобы уменьшить
 * затраты на память (в три раза! =)), но обращение к этой опции все равно
 * осуществляется с помощью класса ZLColor, который реализует более привычное
 * RGB представление цвета.
 * 
 * @author Администратор
 * 
 */
public final class ZLColorOption extends ZLOption {
	private int myIntValue;

	private final int myDefaultValue;

	public ZLColorOption(String category, String group, String optionName,
			ZLColor defaultValue) {
		super(category, group, optionName);
		if (defaultValue != null) {
			myDefaultValue = defaultValue.getIntValue();
		} else {
			myDefaultValue = 0;
		}
		myIntValue = myDefaultValue;
	}

	public ZLColor getValue() {
		if (!myIsSynchronized) {
			String value = myConfig.getValue(myGroup, myOptionName, null);
			if (value != null) {
				try {
					Integer intValue = Integer.parseInt(value);
					myIntValue = intValue;
					myConfig.setValue(myGroup, myOptionName, value , myCategory);
				} catch (NumberFormatException e) {
					// System.err.println(e);
				}
			}
			myIsSynchronized = true;
		}
		return new ZLColor(myIntValue);
	}

	public void setValue(ZLColor colorValue) {
		if (colorValue != null) {
			int value = colorValue.getIntValue();
			if (myIsSynchronized && (myIntValue == value)) {
				return;
			}
			myIntValue = value;
			myIsSynchronized = true;
			if (myIntValue == myDefaultValue) {
				myConfig.unsetValue(myGroup, myOptionName);
			} else {
				myConfig.setValue(myGroup, myOptionName, "" + myIntValue,
						myCategory);
			}
		}
	}
}
