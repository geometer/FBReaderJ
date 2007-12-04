package org.zlibrary.core.options.config;

import java.util.*;

/*package*/ final class ZLConfigImpl implements ZLConfig {
	
	// public abstract boolean isAutoSavingSupported() const = 0;
	// public abstract void startAutoSave(int seconds) = 0;
	
	private final ZLSimpleConfig myMainConfig;

	private final ZLDeltaConfig myDeltaConfig;

	protected ZLConfigImpl() {
		myMainConfig = new ZLSimpleConfig();
		myDeltaConfig = new ZLDeltaConfig();
	}

	public ZLDeltaConfig getDelta() {
		return myDeltaConfig;
	}

	protected Set<ZLGroup> getGroups() {
		return myMainConfig.getGroups();
	}

	public String getValue(String group, String name, String defaultValue) {
		String value = myDeltaConfig.getValue(group, name, defaultValue);
		if (value == defaultValue) {
			value = myMainConfig.getValue(group, name, defaultValue);
		}
		return value;
	}

	/*protected void setCategory(String group, String name, String cat) {
		myDeltaConfig.setCategory(group, name, cat);
		myMainConfig.setCategory(group, name, cat);
	}*/

	public void removeGroup(String name) {
		myDeltaConfig.removeGroup(name);
	}

	public void setValue(String group, String name, String value,
			String category) {
		myDeltaConfig.setValue(group, name, value, category);
	}

	public void unsetValue(String group, String name) {
		myDeltaConfig.unsetValue(group, name);
	}

	protected void clearDelta() {
		myDeltaConfig.clear();
	}

	protected Set<String> applyDelta() {
		Set<String> usedCategories = new HashSet<String>();
		for (String deletedGroup : myDeltaConfig.getDeletedGroups()) {
			ZLGroup gr = myMainConfig.getGroup(deletedGroup);
			if (gr != null) {
				for (ZLOptionInfo option : gr.getOptions()) {
					if (option.getCategory() != null) {
						usedCategories.add(option.getCategory());
					}
				}
				myMainConfig.removeGroup(deletedGroup);
			}
		}

		Set<String> deletedValues = myDeltaConfig.getDeletedValues()
				.getGroups();
		for (String group : deletedValues) {
			for (String optionName : myDeltaConfig.getDeletedValues().getOptions(
					group)) {
				ZLGroup gr = myMainConfig.getGroup(group);
				if (gr != null) {
                    for (ZLOptionInfo option : gr.getOptions()) {
                        if (option.getName().equals(optionName)) {
                            usedCategories.add(myMainConfig.getCategory(group,
                                    optionName));
                            //System.out.println("unset");
                            myMainConfig.unsetValue(group, optionName);
                        }
                    }
				}
			}
		}

		ZLSimpleConfig setValues = myDeltaConfig.getSetValues();
		Set<ZLGroup> groups = setValues.getGroups();
		for (ZLGroup group : groups) {
			for (ZLOptionInfo value : group.getOptions()) {
				usedCategories.add(value.getCategory());
				myMainConfig.setValue(group.getName(), value.getName(), value.getValue(),
						value.getCategory());
			}
		}
		myDeltaConfig.clear();
		return usedCategories;
	}
}
