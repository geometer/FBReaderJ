package org.zlibrary.options.impl;

import org.zlibrary.options.OptionType;

public final class ZLStringOption extends ZLSimpleOptionImpl {
	
	
	private String myValue;
	private String myDefaultValue;
	
	public OptionType getType(){
		return OptionType.TYPE_STRING;
	}

	public String getValue(){
		return myValue;
	}
	
	public void setValue(String value){
		
	}
	
	public ZLStringOption (String category, String group, String optionName, String defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
	}
	
}
