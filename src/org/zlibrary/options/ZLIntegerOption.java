package org.zlibrary.options;

public final class ZLIntegerOption extends ZLOption{
	private long myValue;
	private long myDefaultValue;
	
	public long getValue(){
		return myValue;
	}
	
	public void setValue(long value){
//		TODO установка значения
	}
	
	public ZLIntegerOption (String category, String group, String optionName, int defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}
	
}
