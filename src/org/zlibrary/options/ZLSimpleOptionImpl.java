package org.zlibrary.options;


abstract class ZLSimpleOption extends ZLOption {

	public abstract OptionType getType();

	public ZLSimpleOption(String category, String group, String optionName){
		super(category, group, optionName);
	}

	/*public void clearGroup(String group) {
		// TODO Auto-generated method stub

	}*/

}
