package org.zlibrary.options;

/**
 * класс логическая опция.
 * @author Администратор
 *
 */
public final class ZLBooleanOption extends ZLSimpleOption{
	
	private boolean myValue;
	private boolean myDefaultValue;
	
	public ZLOptionType getType(){
		return ZLOptionType.TYPE_BOOLEAN;
	}

	public boolean getValue(){
		return myValue;
	}
	
	public void setValue(boolean value){
		myValue = value;
	}

	public void setValueToDefault(){
		myValue = myDefaultValue;
	}
	
	public ZLBooleanOption (String category, String group, String optionName, boolean defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}

}
