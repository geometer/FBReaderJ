package org.zlibrary.options;

import org.zlibrary.options.util.*;

/**
 * класс опция со значением из трехзначной логики
 * значения бывают "истина", "ложь" и "не знаю".
 * @author Администратор
 *
 */
public final class ZLBoolean3Option extends ZLSimpleOption{
	
	private	ZLBoolean3 myValue;
	private	ZLBoolean3 myDefaultValue;
	
	public OptionType getType(){
		return OptionType.TYPE_BOOLEAN3;
	}

	public ZLBoolean3 getValue(){
		return myValue;
	}
	
	public void setValue(ZLBoolean3 value){
		myValue = value;
	}
	
	public void setValueToDefault(){
		myValue = myDefaultValue;
	}
	
	public ZLBoolean3Option(String category, String group, String optionName, ZLBoolean3 defaultValue){		
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}
}
