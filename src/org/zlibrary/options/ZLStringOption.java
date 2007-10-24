package org.zlibrary.options;


public final class ZLStringOption extends ZLSimpleOption {
	
	
	private String myValue;
	private String myDefaultValue;
	
	public OptionType getType(){
		return OptionType.TYPE_STRING;
	}

	public String getValue(){
		return myValue;
	}
	
	public void setValue(String value){
//		TODO установка значения
	}
	
	public ZLStringOption (String category, String group, String optionName, String defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}
	
}
