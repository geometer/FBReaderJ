package org.zlibrary.options;

import org.zlibrary.options.util.ZLFromStringConverter;
import org.zlibrary.options.util.ZLToStringConverter;

/**
 * класс ранжированная целочисленная опция. есть верхний и нижний
 * пределы, которые тут же и указываются.
 * @author Администратор
 *
 */
public final class ZLIntegerRangeOption extends ZLOption {
	
    private long myValue;
	private long myDefaultValue;
	private long myMinValue;
	private long myMaxValue;
	
	public long getMinValue(){
		return myMinValue;
	}
	
	public long getMaxValue(){
		return myMaxValue;
	}
	
    public long getValue(){
        if (!myIsSynchronized){
            String strDefaultValue = ZLToStringConverter.convert(myDefaultValue);
            String value = myConfig.getValue(myCategory, myGroup, 
                    myOptionName, strDefaultValue);
            myValue = ZLFromStringConverter.getLongValue(value);
            myIsSynchronized = true;
        }
        return myValue;
    }
    
    public void setValue(long value){
        if ((value <= myMaxValue) && (value >= myMinValue)) {
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
	
	public ZLIntegerRangeOption (String category, String group, String optionName, long minValue, long maxValue, long defaultValue){
		super(category, group, optionName);
		myMinValue = minValue;
		myMaxValue = maxValue;
		//страхуемся от ошибки программиста =)
		if ((myMinValue <= defaultValue) && (myMaxValue >= defaultValue))
			myDefaultValue = defaultValue;
		else
			myDefaultValue = myMinValue;
		myValue = myDefaultValue;
	}
	
}
