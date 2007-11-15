package org.zlibrary.core.application;

import java.util.HashMap;
import java.util.Map;

import org.zlibrary.options.ZLIntegerOption;
import org.zlibrary.options.ZLIntegerRangeOption;
import org.zlibrary.options.ZLOption;
import org.zlibrary.options.ZLStringOption;

public class ZLKeyBindings {
	public static final String BINDINGS_NUMBER = "Number";
	public static final String BINDED_KEY = "Key";
	public static final String BINDED_ACTION = "Action";

	private String myName;
	private Map<String, Integer> myBindingsMap = new HashMap<String, Integer>();
	private	boolean myIsChanged;

    public ZLKeyBindings(String name) {
    	this.myName = name;
    	loadDefaultBindings();
    	loadCustomBindings();
    	myIsChanged = false;
    }
	
	public void bindKey(String key, int code) {
		myBindingsMap.put(key, code);
		myIsChanged = true;
	}
	
	public int getBinding(String key) {
		return myBindingsMap.get(key);
	}

	private void loadDefaultBindings() {
		Map<String,Integer> keymap = new HashMap<String,Integer>();
		new ZLKeyBindingsReader(keymap).readBindings();
		for (Map.Entry<String,Integer> entry: myBindingsMap.entrySet()) {
			bindKey(entry.getKey(), entry.getValue());
		}
	}
	
	private	void loadCustomBindings() {
		long size = new ZLIntegerRangeOption(ZLOption.CONFIG_CATEGORY, myName, BINDINGS_NUMBER, 0, 256, 0).getValue();
		for (int i = 0; i < size; ++i) {
			String key = BINDED_KEY;
			//ZLStringUtil.appendNumber(key, i);
			String keyValue = new ZLStringOption(ZLOption.CONFIG_CATEGORY, myName, key, "").getValue();
			//if (!keyValue.empty()) {
			if (keyValue.length() != 0) {
				String action = BINDED_ACTION;
				//ZLStringUtil.appendNumber(action, i);
				int actionValue = (int)(new ZLIntegerOption(ZLOption.CONFIG_CATEGORY, myName, action, -1).getValue());
				if (actionValue != -1) {
					bindKey(keyValue, actionValue);
				}
			}
		}
	}

	public void saveCustomBindings() {
		if (!myIsChanged) {
			return;
		}
		
		Map<String,Integer> keymap = new HashMap<String,Integer>();
		new ZLKeyBindingsReader(keymap).readBindings();
		
		//ZLOption.clearGroup(myName);
		int counter = 0;
		for (Map.Entry<String,Integer> entry: myBindingsMap.entrySet()) {
			Integer original = keymap.get(entry.getKey());
			int defaultAction = original;//(original == keymap.end()) ? 0 : original.getValue();
			if (defaultAction != entry.getValue()) {
				String key = BINDED_KEY;
				//ZLStringUtil.appendNumber(key, counter);
				String action = BINDED_ACTION;
				//ZLStringUtil.appendNumber(action, counter);
				new ZLStringOption(ZLOption.CONFIG_CATEGORY, myName, key, "").setValue(entry.getKey());
				new ZLIntegerOption(ZLOption.CONFIG_CATEGORY, myName, action, -1).setValue(entry.getValue());
				++counter;
			}
		}
		new ZLIntegerRangeOption(ZLOption.CONFIG_CATEGORY, myName, BINDINGS_NUMBER, 0, 256, 0).setValue(counter);        	
	}
}
