package org.zlibrary.options;

import org.zlibrary.options.util.*;

/**
 * класс опция цвета. внутри опции цвет хранится одним числом,
 * чтобы уменьшить затраты на память (в три раза! =)), но обращение
 * к этой опции все равно осуществляется с помощью класса ZLColor, который
 * реализует более привычное RGB представление цвета. 
 * @author Администратор
 *
 */
public final class ZLColorOption extends ZLOption{
	
	private long myIntValue;
	private long myDefaultIntValue;
	
	public long getValue(){
		return myIntValue;
	}
	
	public void setValue(ZLColor value){
		myIntValue = value.getIntValue();
	}

	public void setValueToDefault(){
		myIntValue = myDefaultIntValue;
	}
	
	public ZLColorOption (String category, String group, String optionName, ZLColor defaultValue){
		super(category, group, optionName);
		myDefaultIntValue = defaultValue.getIntValue();
		myIntValue = myDefaultIntValue;
	}
	
}
