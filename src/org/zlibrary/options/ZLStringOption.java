package org.zlibrary.options;

/**
 * класс строковая опция.
 * @author Администратор
 */
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
		myValue = value;
	}
	
	public void setValueToDefault(){
		myValue = myDefaultValue;
	}
	
	public ZLStringOption (String category, String group, String optionName, String defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}
	
}
