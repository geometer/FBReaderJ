package org.zlibrary.options;

import org.zlibrary.options.util.*;

/**
 * класс опция со значением из трехзначной логики
 * значения бывают "истина", "ложь" и "не знаю".
 * @author Администратор
 *
 */
public final class ZLBoolean3Option extends ZLSimpleOption {
	
	private	ZLBoolean3 myValue;
	private	ZLBoolean3 myDefaultValue;
	
    public ZLBoolean3Option(String category, String group, String optionName, ZLBoolean3 defaultValue){     
        super(category, group, optionName);
        myDefaultValue = defaultValue;
        myValue = myDefaultValue;
    }
    
	public ZLOptionType getType(){
		return ZLOptionType.TYPE_BOOLEAN3;
	}
	
	public ZLBoolean3 getValue(){
        if (!myIsSynchronized){
            String strDefaultValue = ZLToStringConverter.convert(myDefaultValue);
            String value = myConfig.getValue(myCategory, myGroup, 
                    myOptionName, strDefaultValue);
            
            myValue = ZLFromStringConverter.getBoolean3Value(value);
            myIsSynchronized = true;
        }
		return myValue;
	}
	
	public void setValue(ZLBoolean3 value){
        if (myIsSynchronized && (myValue == value)) {
            return;
        }
        myValue = value;
        myIsSynchronized = true;
        if (myValue == myDefaultValue) {
            myConfig.unsetValue(myCategory, myGroup, myOptionName);
        } else {
            String stringValue = ZLToStringConverter.convert(myValue);
            myConfig.setValue(myCategory, myOptionName, myGroup, stringValue);
        }
	}
}
