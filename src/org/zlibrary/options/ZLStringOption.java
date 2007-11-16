package org.zlibrary.options;


/**
 * класс строковая опция.
 * @author Администратор
 */
public final class ZLStringOption extends ZLSimpleOption {
	private String myValue;
	private final String myDefaultValue;
	
	public ZLStringOption(String category, String group, String optionName, String defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}
	
	public ZLOptionType getType(){
		return ZLOptionType.TYPE_STRING;
	}

	public String getValue(){
		if (!myIsSynchronized){
			String value = myConfig.getValue(myCategory, myGroup, 
					myOptionName, myDefaultValue);
			myValue = value;
			myIsSynchronized = true;
		}
		return myValue;
	}
	
	public void setValue(String value){
		if (myIsSynchronized && (myValue == value)) {
			return;
		}
		myValue = value;
		myIsSynchronized = true;
		if (myValue == myDefaultValue) {
			myConfig.unsetValue(myCategory, myGroup, myOptionName);
		} else {
			myConfig.setValue(myCategory, myGroup, myOptionName, myValue);
		}
	}
}
