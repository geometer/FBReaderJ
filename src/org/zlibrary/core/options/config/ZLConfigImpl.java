package org.zlibrary.core.options.config;

import java.util.Map;

/*package*/ class ZLConfigImpl implements ZLConfig {
	private final ZLSimpleConfigImpl myMainConfig;
	private final ZLDeltaConfig myDeltaConfig;

	public ZLConfigImpl() {
		myMainConfig = new ZLSimpleConfigImpl();
		myDeltaConfig = new ZLDeltaConfig();
	}

	public ZLDeltaConfig getDelta() {
		return myDeltaConfig;
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
	
	public void setCategory(String group, String name, String cat) {
		myDeltaConfig.setCategory(group, name, cat);
		myMainConfig.setCategory(group, name, cat);
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
		
		Map<String, ZLGroup> deletedValues = myDeltaConfig.getDeletedValues().getGroups();
		for (String group : deletedValues.keySet()) {
			for (ZLOptionValue option : deletedValues.get(group).getValues()) {
				myMainConfig.unsetValue(group, option.getName());
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
