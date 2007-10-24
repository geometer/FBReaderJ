package org.zlibrary.options;

public final class ZLBooleanOption extends ZLSimpleOption{
	
	private boolean myValue;
	private boolean myDefaultValue;
	
	public OptionType getType(){
		return OptionType.TYPE_BOOLEAN;
	}

	public boolean getValue(){
		return true;
	}
	
	public void setValue(boolean value){
		
	}
	
	public ZLBooleanOption (String category, String group, String optionName, boolean defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
	}

}
