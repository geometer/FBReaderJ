package org.zlibrary.options.impl;

import org.zlibrary.options.*;

abstract class ZLOptionImpl implements ZLOption{
	
	public String myCategory;
	public String myGroup;
	public String myOptionName;
	public boolean myIsSynchronized;

	public void clearGroup(String group){
		
	}
	
	//private final ZLOption& operator = (final ZLOptions options);
	
	public ZLOptionImpl (String category, String group, String optionName){
		
	}
}
