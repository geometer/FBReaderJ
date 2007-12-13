package org.zlibrary.core.options.config;

import java.util.Set;
import java.util.HashMap;

final class ZLGroup {
	private final HashMap<String,ZLOptionInfo> myData = new HashMap<String,ZLOptionInfo>();

	Set<String> optionNames() {
		return myData.keySet();
	}

	ZLOptionInfo getOption(String name) {
		return myData.get(name);
	}

	String getValue(String name) {
		ZLOptionInfo info = myData.get(name);
		return (info != null) ? info.getValue() : null;
	}

	void setValue(String name, String value, String category) {
		ZLOptionInfo info = myData.get(name);
		if (info != null) {
			info.setValue(value);
			info.setCategory(category);
		} else {
			myData.put(name, new ZLOptionInfo(name, value, category));
		}
	}

	public void unsetValue(String name) {
		myData.remove(name);
	}
}
