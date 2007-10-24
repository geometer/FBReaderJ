package org.zlibrary.options;

public final class ZLIntegerRangeOption extends ZLOption{
	private long myValue;
	private long myDefaultValue;
	private long myMinValue;
	private long myMaxValue;
	
	public long getValue(){
		return myValue;
	}
	
	public long getMinValue(){
		return myMinValue;
	}
	
	public long getMaxValue(){
		return myMaxValue;
	}
	
	public void setValue(long value){
//		TODO установка значения
	}
	
	public ZLIntegerRangeOption (String category, String group, String optionName, long minValue, long maxValue, long defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myMinValue = minValue;
		myMaxValue = maxValue;
		myValue = myDefaultValue;
	}
	
}
