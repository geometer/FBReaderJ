package org.zlibrary.options;

/**
 * класс целочисленная опция.
 * @author Администратор
 *
 */
public final class ZLIntegerOption extends ZLOption{
	private long myValue;
	private long myDefaultValue;
	
	public long getValue(){
		return myValue;
	}
	
	public void setValue(long value){
		myValue = value;
	}

	public void setValueToDefault(){
		myValue = myDefaultValue;
	}
	
	public ZLIntegerOption (String category, String group, String optionName, long defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}
	
}
