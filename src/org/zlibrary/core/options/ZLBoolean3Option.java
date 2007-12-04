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
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}
	
	public ZLOptionType getType() {
		return ZLOptionType.TYPE_BOOLEAN3;
	}
	
	public ZLBoolean3 getValue() {
		if (!myIsSynchronized) {
			String value = myConfig.getValue(myGroup, myOptionName,	null);
			if (value != null) {
				if (value.toLowerCase().equals("true")) {
					myValue = ZLBoolean3.B3_TRUE;
				} else if (value.toLowerCase().equals("false")) {
					myValue = ZLBoolean3.B3_FALSE;
				} else if (value.toLowerCase().equals("undefined")) {
					myValue = ZLBoolean3.B3_UNDEFINED;
				}
			}
			myIsSynchronized = true;
		}
		return myValue;
	}
	
	public void setValue(ZLBoolean3 value) {
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		myIsSynchronized = true;
		
		if (myValue == myDefaultValue) {
		myConfig.unsetValue(myGroup, myOptionName);
		} else {
			myConfig.setValue(myOptionName, myGroup, myValue.toString(), myCategory);
		}
	}
}
