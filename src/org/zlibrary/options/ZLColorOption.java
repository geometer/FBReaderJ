package org.zlibrary.options;

import org.zlibrary.options.util.*;

public final class ZLColorOption extends ZLOption{
	
	private long myIntValue;
	private long myDefaultIntValue;
	
	public long getValue(){
		return myIntValue;
	}
	
	public void setValue(ZLColor value){
		//TODO установка значения
	}
	
	public ZLColorOption (String category, String group, String optionName, ZLColor defaultValue){
		super(category, group, optionName);
		//convert?
		myDefaultIntValue = defaultValue.getIntValue();
		myIntValue = myDefaultIntValue;
	}
	
}
