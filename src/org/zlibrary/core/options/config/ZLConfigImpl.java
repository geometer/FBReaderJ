package org.zlibrary.core.options.config;

import java.util.Map;

/*package*/ class ZLConfigImpl implements ZLConfig {
	private final ZLSimpleConfigImpl myMainConfig;
	private final ZLDeltaConfig myDeltaConfig;

	public ZLConfigImpl() {
		myMainConfig = new ZLSimpleConfigImpl();
		myDeltaConfig = new ZLDeltaConfig();
	}

	public Map<String, ZLGroup> getGroups() {
		return myMainConfig.getGroups();
	}

	public String getValue(String group, String name, String defaultValue) {
		String value = myDeltaConfig.getValue(group, name, defaultValue);
		if (value == defaultValue) {
		    value = myMainConfig.getValue(group, name, defaultValue);
		}
        return value;
	}

	public void removeGroup(String name) {
		myDeltaConfig.removeGroup(name);
	}

	public void setValue(String group, String name, String value, String category) {
		myDeltaConfig.setValue(group, name, value, category);
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
		
		Map<String, String> deletedValues = myDeltaConfig.getDeletedValues();
		for (String group : deletedValues.keySet()) {
			myMainConfig.unsetValue(group, deletedValues.get(group));
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
