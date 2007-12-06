package org.zlibrary.core.options.config;

import java.util.*;

/*package*/ final class ZLGroup {
	private Set<ZLOptionInfo> myData;

	private final String myName;

	public ZLGroup(String name) {
		myData = new HashSet<ZLOptionInfo>();
		myName = name;
	}

	public String getName() {
		return myName;
	}

	public Set<ZLOptionInfo> getOptions() {
		return Collections.unmodifiableSet(myData);
	}

	protected ZLOptionInfo getOption(String name) {
		for (ZLOptionInfo option : myData) {
			if (option.getName().equals(name)) {
				return option;
			}
		}
		return null;
	}

	public String getValue(String name) {
		ZLOptionInfo temp = getOption(name);
		if (temp != null) {
			return temp.getValue();
		} else {
			return null;
		}
	}

	public void setValue(String name, String value, String category) {
		ZLOptionInfo temp = getOption(name);
		if (temp == null) {
			myData.add(new ZLOptionInfo(name, value, category));
		} else {
			temp.setValue(value);
			temp.setCategory(category);
		}
	}

	
	//TODO cuncurrent modification
	public void unsetValue(String name) {
		for (ZLOptionInfo option : myData) {
			if (option.getName().equals(name)) {
				myData.remove(option);
			}
		}
	}
}
