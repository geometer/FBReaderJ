package org.zlibrary.options;

public final class ZLIntegerRangeOption extends ZLOption{
	private long myValue;
	private long myDefaultValue;
	private long myMinValue;
	private long myMaxValue;
	
	public long getValue(){
		return 0;
	}
	
	public long getMinValue(){
		return 0;
	}
	
	public long getMaxValue(){
		return 0;
	}
	
	public void setValue(long value){
		
	}
	
	public ZLIntegerRangeOption (String category, String group, String optionName, long minValue, long maxValue, long defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myMinValue = minValue;
		myMaxValue = maxValue;
	}
	
}
