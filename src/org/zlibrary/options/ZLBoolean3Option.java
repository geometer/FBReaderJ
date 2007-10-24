package org.zlibrary.options;

import org.zlibrary.options.*;
import org.zlibrary.options.util.*;

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
		
	}
	
	public ZLBoolean3Option(String category, String group, String optionName, ZLBoolean3 defaultValue){		
		super(category, group, optionName);
		myDefaultValue = defaultValue;
	}
}
