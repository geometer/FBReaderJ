package org.zlibrary.core.application;

import java.util.Collections;
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;

import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.options.ZLIntegerRangeOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.options.ZLStringOption;

public final class ZLKeyBindings {
	private static final String BINDINGS_NUMBER = "Number";
	private static final String BINDED_KEY = "Key";
	private static final String BINDED_ACTION = "Action";

	private final String myName;
	private final Map<String, Integer> myBindingsMap = new TreeMap<String, Integer>();
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
	
	public int getBinding(String key) {
		Integer num = myBindingsMap.get(key);
		return (num == null) ? 0 : num;
	}
	
	public Set<String> getKeys() {
		return myBindingsMap.keySet();
	}

	private void loadDefaultBindings() {
		TreeMap<String,Integer> keymap = new TreeMap<String,Integer>();
		new ZLKeyBindingsReader(keymap).readBindings();
		for (Map.Entry<String,Integer> entry: keymap.entrySet()) {
			bindKey(entry.getKey(), entry.getValue());
		}
	}
	
	private	void loadCustomBindings() {
		final int size =
			new ZLIntegerRangeOption(ZLOption.CONFIG_CATEGORY, myName, BINDINGS_NUMBER, 0, 256, 0).getValue();
		final ZLStringOption keyOption =
			new ZLStringOption(ZLOption.CONFIG_CATEGORY, myName, "", "");
		final ZLIntegerOption actionOption =
			new ZLIntegerOption(ZLOption.CONFIG_CATEGORY, myName, "", -1);
		for (int i = 0; i < size; ++i) {
			keyOption.changeName(BINDED_KEY + i);
			String keyValue = keyOption.getValue();
			if (keyValue.length() != 0) {
				keyOption.changeName(BINDED_ACTION + i);
				int actionValue = actionOption.getValue();
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
		
		TreeMap<String,Integer> keymap = new TreeMap<String,Integer>();
		new ZLKeyBindingsReader(keymap).readBindings();
		
		int counter = 0;
		final ZLStringOption keyOption =
			new ZLStringOption(ZLOption.CONFIG_CATEGORY, myName, "", "");
		final ZLIntegerOption actionOption =
			new ZLIntegerOption(ZLOption.CONFIG_CATEGORY, myName, "", -1);
		for (Map.Entry<String,Integer> entry : myBindingsMap.entrySet()) {
			Integer original = keymap.get(entry.getKey());
			int defaultAction = original;
			if (defaultAction != entry.getValue()) {
				keyOption.changeName(BINDED_KEY + counter);
				actionOption.changeName(BINDED_ACTION + counter);
				keyOption.setValue(entry.getKey());
				actionOption.setValue(entry.getValue());
				++counter;
			}
		}
		new ZLIntegerRangeOption(ZLOption.CONFIG_CATEGORY, myName, BINDINGS_NUMBER, 0, 256, 0).setValue(counter);
	}
}
