package org.zlibrary.options;

import org.zlibrary.options.util.*;

/**
 * класс опция цвета. внутри опции цвет хранится одним числом,
 * чтобы уменьшить затраты на память (в три раза! =)), но обращение
 * к этой опции все равно осуществляется с помощью класса ZLColor, который
 * реализует более привычное RGB представление цвета. 
 * @author Администратор
 *
 */
public final class ZLColorOption extends ZLOption {
	private long myIntValue;
	private final long myDefaultValue;
	
	public ZLColorOption (String category, String group, String optionName, ZLColor defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue.getIntValue();
		myIntValue = myDefaultValue;
	}
	
	public long getValue(){
		if (!myIsSynchronized){
			String strDefaultValue = ZLToStringConverter.convert(myDefaultValue);
			String value = myConfig.getValue(myCategory, myGroup, myOptionName, strDefaultValue);
			myIntValue = ZLFromStringConverter.getIntegerValue(value);
			myIsSynchronized = true;
		}
		return myIntValue;
	}
	
	public void setValue(ZLColor colorValue){
		long value = colorValue.getIntValue();
		if (myIsSynchronized && (myIntValue == value)) {
			return;
		}
		myIntValue = value;
		myIsSynchronized = true;
		if (myIntValue == myDefaultValue) {
			myConfig.unsetValue(myCategory, myGroup, myOptionName);
		} else {
			String stringValue = ZLToStringConverter.convert(myIntValue);
			myConfig.setValue(myCategory, myGroup, myOptionName, stringValue);
		}
	}	
}
