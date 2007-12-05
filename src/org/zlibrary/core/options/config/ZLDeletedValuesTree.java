package org.zlibrary.core.options.config;

import java.util.*;

/*package*/ final class ZLDeletedValuesTree {
	
	private Map<String, List<String>> myData = 
		new TreeMap<String, List<String>>();

	public void add(String group, String name) {
		if (myData.get(group) == null) {
			myData.put(group, new ArrayList<String>());
		}
		myData.get(group).add(name);
	}

	public boolean contains(String group, String name) {
		if (myData.get(group) != null) {
			return myData.get(group).contains(name.intern());
		} else {
			return false;
		}
	}
	
	public Set<String> getGroups() {
		return Collections.unmodifiableSet(myData.keySet());
	}

	public List<String> getOptions(String group) {
		if (myData.get(group) != null) {
			return Collections.unmodifiableList(myData.get(group));
		} else {
			return null;
		}
	}

	public void clear() {
		myData.clear();
	}
}
