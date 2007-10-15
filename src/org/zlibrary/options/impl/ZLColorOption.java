package org.zlibrary.options.impl;

import org.zlibrary.options.util.*;

public final class ZLColorOption extends ZLOptionImpl{
	
	private long myIntValue;
	private long myDefaultIntValue;
	
	public long getValue(){
		return 0;
	}
	
	public void setValue(ZLColor value){
		
	}
	
	public ZLColorOption (String category, String group, String optionName, ZLColor defaultValue){
		super(category, group, optionName);
		
		//convert?
		//myDefaultIntValue = defaultValue;
	}
	
}
