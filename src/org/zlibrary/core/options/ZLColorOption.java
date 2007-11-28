package org.zlibrary.core.options;

import org.zlibrary.core.options.util.*;

/**
 * класс опция цвета. внутри опции цвет хранится одним числом,
 * чтобы уменьшить затраты на память (в три раза! =)), но обращение
 * к этой опции все равно осуществляется с помощью класса ZLColor, который
 * реализует более привычное RGB представление цвета. 
 * @author Администратор
 *
 */
public final class ZLColorOption extends ZLOption {
	private int myIntValue;
	private final int myDefaultValue;
	
	public ZLColorOption (String category, String group, String optionName, ZLColor defaultValue) {
		super(category, group, optionName);
		myDefaultValue = defaultValue.getIntValue();
		myIntValue = myDefaultValue;
	}
	
	public int getValue() {
		if (!myIsSynchronized) {
			String strDefaultValue = ((Integer)myDefaultValue).toString();
			String value = myConfig.getValue(myGroup, myOptionName, strDefaultValue);
			myIntValue = ZLFromStringConverter.getIntegerValue(value);
			myIsSynchronized = true;
		}
		return myIntValue;
	}
	
	public void setValue(ZLColor colorValue) {
		int value = colorValue.getIntValue();
		if (myIsSynchronized && (myIntValue == value)) {
			return;
		}
		myIntValue = value;
		myIsSynchronized = true;
		if (myIntValue == myDefaultValue) {
			myConfig.unsetValue(myGroup, myOptionName);
		} else {
			String stringValue = ((Integer)myIntValue).toString();
			myConfig.setValue(myGroup, myOptionName, stringValue, myCategory);
		}
	}	
}
