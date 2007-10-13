package org.zlibrary.options.impl;

import org.zlibrary.options.OptionType;
import org.zlibrary.options.ZLSimpleOption;

public abstract class ZLSimpleOptionImpl extends ZLOptionImpl implements ZLSimpleOption {

	public abstract OptionType getType();

	public ZLSimpleOptionImpl(String category, String group, String optionName){
		super(category, group, optionName);
	}

	/*public void clearGroup(String group) {
		// TODO Auto-generated method stub

	}*/

}
