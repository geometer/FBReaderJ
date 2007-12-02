package org.zlibrary.core.options.config;

import java.util.*;

/*package*/ class ZLConfigImpl implements ZLConfig {
	private final ZLSimpleConfig myMainConfig;
	private final ZLDeltaConfig myDeltaConfig;
    
	public ZLConfigImpl() {
		myMainConfig = new ZLSimpleConfig();
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
	
	public Set<String> applyDelta() {
        Set<String> usedCategories = new HashSet<String>();
		for (String deletedGroup : myDeltaConfig.getDeletedGroups()) {
            if (myMainConfig.getGroups().keySet().contains(deletedGroup)) {
                for (ZLOptionValue option : 
                    myMainConfig.getGroups().get(deletedGroup).getValues()) {
                    if (option.getCategory() != null) {
                        usedCategories.add(option.getCategory());
                    }
                }
                myMainConfig.removeGroup(deletedGroup);
            }
		}
		
		Set<String> deletedValues = myDeltaConfig.getDeletedValues().getGroups();
		for (String group : deletedValues) {
            for (String option : myDeltaConfig.getDeletedValues().getOptions(group)) {
                ZLGroup zlgroup = myMainConfig.getGroups().get(group);
                if (zlgroup != null) {
                    if (zlgroup.getValues().contains(option)) {
                        usedCategories.add(myMainConfig.getCategory(group, option));
                        myMainConfig.unsetValue(group, option);
                    }
                }
            }
		}
		
		ZLSimpleConfig setValues = myDeltaConfig.getSetValues();
		Map<String, ZLGroup> groups = setValues.getGroups();
		for (String group : groups.keySet()) {
			for (ZLOptionValue value : groups.get(group).getValues()) {
                usedCategories.add(value.getCategory());
				myMainConfig.setValue(group, value.getName(), 
						value.getValue(), value.getCategory());
			}
		}
		myDeltaConfig.clear();
        return usedCategories;
	}
}
