package org.zlibrary.core.options.config;

import java.util.*;

final class ZLGroup {
	private final HashMap<String,ZLOptionInfo> myData = new HashMap<String,ZLOptionInfo>();
	private final String myName;

	ZLGroup(String name) {
		myName = name;
	}

	String getName() {
		return myName;
	}

	Collection<ZLOptionInfo> options() {
		return myData.values();
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
