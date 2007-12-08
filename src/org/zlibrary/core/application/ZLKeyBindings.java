package org.zlibrary.core.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.options.ZLIntegerRangeOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.options.ZLStringOption;


public class ZLKeyBindings {
	public static final String BINDINGS_NUMBER = "Number";
	public static final String BINDED_KEY = "Key";
	public static final String BINDED_ACTION = "Action";

	private final String myName;
	private final Map<String, Integer> myBindingsMap = new HashMap<String, Integer>();
	private	boolean myIsChanged;

	public ZLKeyBindings(String name) {
		myName = name;
		loadDefaultBindings();
		loadCustomBindings();
		myIsChanged = false;
	}
	
	public void bindKey(String key, int code) {
		myBindingsMap.put(key, code);
		myIsChanged = true;
	}
	
	public Integer getBinding(String key) {
		return myBindingsMap.get(key);
	}
	
	public Set<Map.Entry<String, Integer>> getKeys() {
		return Collections.unmodifiableSet(myBindingsMap.entrySet());
	}

	private void loadDefaultBindings() {
		Map<String,Integer> keymap = new HashMap<String,Integer>();
		new ZLKeyBindingsReader(keymap).readBindings();
		for (Map.Entry<String,Integer> entry: keymap.entrySet()) {
			bindKey(entry.getKey(), entry.getValue());
		}
	}
	
	private	void loadCustomBindings() {
		long size = new ZLIntegerRangeOption(ZLOption.CONFIG_CATEGORY, myName, BINDINGS_NUMBER, 0, 256, 0).getValue();
		for (int i = 0; i < size; ++i) {
			String key = BINDED_KEY + i;
			String keyValue = new ZLStringOption(ZLOption.CONFIG_CATEGORY, myName, key, "").getValue();
			if (keyValue.length() != 0) {
				String action = BINDED_ACTION + i;
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
		
		int counter = 0;
		for (Map.Entry<String,Integer> entry: myBindingsMap.entrySet()) {
			Integer original = keymap.get(entry.getKey());
			int defaultAction = original;
			if (defaultAction != entry.getValue()) {
				String key = BINDED_KEY + counter;
				String action = BINDED_ACTION + counter;
				new ZLStringOption(ZLOption.CONFIG_CATEGORY, myName, key, "").setValue(entry.getKey());
				new ZLIntegerOption(ZLOption.CONFIG_CATEGORY, myName, action, -1).setValue(entry.getValue());
				++counter;
			}
		}
		new ZLIntegerRangeOption(ZLOption.CONFIG_CATEGORY, myName, BINDINGS_NUMBER, 0, 256, 0).setValue(counter);
	}
}
