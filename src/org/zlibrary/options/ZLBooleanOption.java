package org.zlibrary.options;

import org.zlibrary.options.util.ZLFromStringConverter;
import org.zlibrary.options.util.ZLToStringConverter;

/**
 * класс логическая опция.
 * @author Администратор
 *
 */
public final class ZLBooleanOption extends ZLSimpleOption {
	
	private boolean myValue;
	private boolean myDefaultValue;
	
    public ZLBooleanOption (String category, String group, String optionName, boolean defaultValue){
        super(category, group, optionName);
        myDefaultValue = defaultValue;
        myValue = myDefaultValue;
    }
    
	public ZLOptionType getType(){
		return ZLOptionType.TYPE_BOOLEAN;
	}

	public boolean getValue(){
        if (!myIsSynchronized){
            String strDefaultValue = ZLToStringConverter.convert(myDefaultValue);
            String value = myConfig.getValue(myCategory, myGroup, 
                    myOptionName, strDefaultValue);
            myValue = ZLFromStringConverter.getBooleanValue(value);
            myIsSynchronized = true;
        }
        return myValue;
	}
	
	public void setValue(boolean value){
        if (myIsSynchronized && (myValue == value)) {
            return;
        }
        myValue = value;
        myIsSynchronized = true;
        if (myValue == myDefaultValue) {
            myConfig.unsetValue(myCategory, myGroup, myOptionName);
        } else {
            String stringValue = ZLToStringConverter.convert(myValue);
            myConfig.setValue(myCategory, myGroup, myOptionName, stringValue);
        }
	}
}
