package org.zlibrary.core.options.config;

import java.util.*;

/*package*/ final class ZLDeltaConfig {
	
	private Map<String, String> myDeletedValues = new HashMap<String, String>();
	private Set<String> myDeletedGroups = new HashSet<String>();
	private ZLSimpleConfigImpl mySetValues = new ZLSimpleConfigImpl();
	
	public ZLDeltaConfig() {
	}
	
	public Set<String> getDeletedGroups() {
		return Collections.unmodifiableSet(myDeletedGroups);
	}
	
	public Map<String, String> getDeletedValues() {
		return Collections.unmodifiableMap(myDeletedValues);
	}
	
	public ZLSimpleConfigImpl getSetValues() {
		return mySetValues;
	}
	
	public String getValue(String group, String name, String defaultValue) {
		return mySetValues.getValue(group, name, defaultValue);
	}
	
	public void setValue(String group, String name, String value, String category) {
		mySetValues.setValue(group, name, value, category);
	}
	
	public void unsetValue(String group, String name) {
		myDeletedValues.put(group, name);
		mySetValues.unsetValue(group, name);
	}
	
	public void removeGroup(String group) {
		myDeletedGroups.add(group);
		mySetValues.removeGroup(group);
	}
	
	public void clear() {
		myDeletedValues.clear();
		myDeletedGroups.clear();
		mySetValues.clear();
	}
}
