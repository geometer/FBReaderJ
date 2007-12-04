package org.zlibrary.core.options.config;

import java.util.*;

/*package*/ final class ZLDeletedValuesTree {
	
	private final Map<String, List<String>> myData = 
		new TreeMap<String, List<String>>();

	public void add(String group, String name) {
		if (myData.get(group) == null) {
			myData.put(group, new ArrayList<String>());
		}
		myData.get(group).add(name);
	}

	public Set<String> getGroups() {
		return Collections.unmodifiableSet(myData.keySet());
	}

	public List<String> getOptions(String group) {
		return Collections.unmodifiableList(myData.get(group));
	}

	public void clear() {
		myData.clear();
	}
}
