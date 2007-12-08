package org.zlibrary.core.options;

import org.zlibrary.core.options.util.*;

/**
 * класс опция со значением из трехзначной логики
 * значения бывают "истина", "ложь" и "не знаю".
 * @author Администратор
 *
 */
public final class ZLBoolean3Option extends ZLSimpleOption {

	private	ZLBoolean3 myValue;
	private	final ZLBoolean3 myDefaultValue;
	
	public ZLBoolean3Option(String category, String group, String optionName, ZLBoolean3 defaultValue) {	 
		super(category, group, optionName);
		if (defaultValue != null) {
			myDefaultValue = defaultValue;
		} else {
			myDefaultValue = ZLBoolean3.B3_UNDEFINED;
		}
		myValue = myDefaultValue;
	}
	
	public ZLOptionType getType() {
		return ZLOptionType.TYPE_BOOLEAN3;
	}
	
	public ZLBoolean3 getValue() {
		if (!myIsSynchronized) {
			String value = myConfig.getValue(myGroup, myOptionName,	null);
			if (value != null) {
				myValue = ZLBoolean3.getByString(value);
			}
			myIsSynchronized = true;
		}
		return myValue;
	}
	
	public void setValue(ZLBoolean3 value) {
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		if (value != null) {
			myValue = value;
			myIsSynchronized = true;
		
			if (myValue == myDefaultValue) {
				myConfig.unsetValue(myGroup, myOptionName);
			} else {
				myConfig.setValue(myGroup, myOptionName, myValue + "", myCategory);
			}
		}
	}
}
