package org.zlibrary.options;

public final class ZLIntegerOption extends ZLOption{
	private long myValue;
	private long myDefaultValue;
	
	public long getValue(){
		return 0;
	}
	
	public void setValue(long value){
		
	}
	
	public ZLIntegerOption (String category, String group, String optionName, int defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
	}
	
}
