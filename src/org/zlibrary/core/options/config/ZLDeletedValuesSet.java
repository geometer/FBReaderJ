package org.zlibrary.core.options.config;

import java.util.*;

/*package*/ final class ZLDeletedValuesSet {
	
	private final Set<ZLOptionID> myData = 
		new LinkedHashSet<ZLOptionID>();

	public void add(String group, String name) {
		myData.add(new ZLOptionID(group, name));
	}

	public Set<String> getGroups() {
		Set<String> temp = new HashSet<String>();
		for (ZLOptionID option : myData) {
			temp.add(option.getGroup());
		}
		return Collections.unmodifiableSet(temp);
	}
	
	public boolean contains(String group, String name) {
		for (ZLOptionID option : myData) {
			if (option.getName().equals(name) 
					&& option.getGroup().equals(group)) {
				return true;
			}
		}
		return false;
	}
	
	public Set<ZLOptionID> getAll() {
		return Collections.unmodifiableSet(myData);
	}

	public void clear() {
		myData.clear();
	}
}
