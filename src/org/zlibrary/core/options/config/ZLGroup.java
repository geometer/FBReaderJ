package org.zlibrary.core.options.config;

import java.util.*;

/*package*/ final class ZLGroup {
	private final Set<ZLOptionInfo> myData;

	private final String myName;

	public ZLGroup(String name) {
		myData = new LinkedHashSet<ZLOptionInfo>();
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
		}
	}

	public void unsetValue(String name) {
		for (ZLOptionInfo option : myData) {
			if (option.getName().equals(name)) {
				myData.remove(name);
			}
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (ZLOptionInfo option : myData) {
			sb.append("    <option name=\"" + option.getName() + "\" value=\""
					+ option + "\"/>\n");
		}
		return sb.toString();
	}
}
