package org.zlibrary.core.options.config;

import java.util.*;

/*package*/ class ZLConfigImpl implements ZLConfig {
	private final ZLSimpleConfigImpl myMainConfig;
	private final ZLDeltaConfig myDeltaConfig;
	private final Set<String> myCategories;
	
	public ZLConfigImpl() {
		myMainConfig = new ZLSimpleConfigImpl();
		myDeltaConfig = new ZLDeltaConfig();
		myCategories = new HashSet<String>();
	}

	public ZLDeltaConfig getDelta() {
		return myDeltaConfig;
	}
	
	public Set<String> getCategories() {
		return Collections.unmodifiableSet(myCategories);
	}
	
	public Map<String, ZLGroup> getGroups() {
		return myMainConfig.getGroups();
	}

	public String getValue(String group, String name, String defaultValue) {
		String value = myDeltaConfig.getValue(group, name, defaultValue);
		if (value == defaultValue) {
			value = myMainConfig.getValue(group, name, 
					defaultValue);
		}
		return value;
	}
	
	public void setCategory(String group, String name, String cat) {
		myDeltaConfig.setCategory(group, name, cat);
		myMainConfig.setCategory(group, name, cat);
	}
	
	public void removeGroup(String name) {
		myDeltaConfig.removeGroup(name);
	}

	public void setValue(String group, String name, String value, String category) {
		myDeltaConfig.setValue(group, name, value, category);
		myCategories.add(category.intern());
	}

	public void unsetValue(String group, String name) {
		myDeltaConfig.unsetValue(group, name);
	}
	
	public void clearDelta() {
		myDeltaConfig.clear();
	}
	
	public void applyDelta() {
		for (String deletedGroup : myDeltaConfig.getDeletedGroups()) {
			myMainConfig.removeGroup(deletedGroup);
		}
		
		ZLDeletedOptionsTree deletedValues = myDeltaConfig.getDeletedValues();
		for (String group : deletedValues.getGroups()) {
			for (String option : deletedValues.getOptions(group)) {
				myMainConfig.unsetValue(group, option);
				System.out.println(option);
				System.out.println(myMainConfig.getValue(group, option, ""));
			}
		}
		
		ZLSimpleConfigImpl setValues = myDeltaConfig.getSetValues();
		Map<String, ZLGroup> groups = setValues.getGroups();
		for (String group : groups.keySet()) {
			for (ZLOptionValue value : groups.get(group).getValues()) {
				myMainConfig.setValue(group, value.getName(), 
						value.getValue(), value.getCategory());
			}
		}
		myDeltaConfig.clear();
	}
}
