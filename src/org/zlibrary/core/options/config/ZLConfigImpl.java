package org.zlibrary.core.options.config;

import java.util.*;

final class ZLConfigImpl implements ZLConfig {
	
	// public abstract boolean isAutoSavingSupported() const = 0;
	// public abstract void startAutoSave(int seconds) = 0;
	
	private final ZLSimpleConfig myMainConfig;

	private final ZLDeltaConfig myDeltaConfig;

	protected ZLConfigImpl() {
		myMainConfig = new ZLSimpleConfig();
		myDeltaConfig = new ZLDeltaConfig();
	}

	//temporary
	protected ZLDeltaConfig getDelta() {
		return myDeltaConfig;
	}

	protected Set<String> groupNames() {
		return myMainConfig.groupNames();
	}

	ZLGroup getGroup(String name) {
		return myMainConfig.getGroup(name);
	}
	
	/**@return defaultValue - when this value is not set or deleted
	 * new value (from setValues) - when it was set
	 * null - when it was deleted
	 */
	public String getValue(String group, String name, String defaultValue) {
		String value = myDeltaConfig.getValue(group, name, defaultValue);
		if (value == null) {
			//System.out.println(name);
			/**
			 * если значение было нулем, то либо опция удалена, либо
			 * нам вернули дефолт
			 * если дефолт не ноль, то опция удалена и возвращаем дефолт
			 * а если ноль, то лезем в основной конфиг и смотрим там
			 */
			if (defaultValue != null) {
				return defaultValue;
			} else {
				return myMainConfig.getValue(group, name, defaultValue);
			}
		} else if (value.equals(defaultValue)) {
			return myMainConfig.getValue(group, name, defaultValue);
		}
		return value;
	}

	public void removeGroup(String name) {
		myDeltaConfig.removeGroup(name);
	}
	
	public void setValue(String group, String name, String value, String category) {
		if (value != null) {
			myDeltaConfig.setValue(group, name, value, category);
		}
	}
	
	protected void setValueDirectly(String group, String name, String value, String category) {
		myMainConfig.setValue(group, name, value, category);
	}

	public void unsetValue(String group, String name) {
		myDeltaConfig.unsetValue(group, name);
	}
	
	protected void clearDelta() {
		myDeltaConfig.clear();
	}

	protected Set<String> applyDelta() {
		HashSet<String> usedCategories = new HashSet<String>();
		for (String deletedGroupName : myDeltaConfig.getDeletedGroups()) {
			ZLGroup group = myMainConfig.getGroup(deletedGroupName);
			if (group!= null) {
				for (String optionName : group.optionNames()) {
					ZLOptionInfo option = group.getOption(optionName);
					if (option.getCategory() != null) {
						usedCategories.add(option.getCategory());
					}
				}
				myMainConfig.removeGroup(deletedGroupName);
			}
		}

		for (ZLOptionID option : myDeltaConfig.getDeletedValues().getAll()) {
			ZLGroup gr = myMainConfig.getGroup(option.getGroup());
			if (gr != null) {
				String cat = myMainConfig.getCategory(option.getGroup(), option.getName());
				if (cat != null) {
					usedCategories.add(cat);
				}
				gr.unsetValue(option.getName());
			}
		}

		ZLSimpleConfig changedValues = myDeltaConfig.changedValues();
		for (String groupName : changedValues.groupNames()) {
			ZLGroup group = changedValues.getGroup(groupName);
			for (String optionName : group.optionNames()) {
				ZLOptionInfo option = group.getOption(optionName);
				usedCategories.add(option.getCategory());
				String cat = myMainConfig.getCategory(groupName, optionName);
				if (cat != null) {
					usedCategories.add(cat);
				}
				myMainConfig.setValue(groupName, optionName, option.getValue(), option.getCategory());
			}
		}
		myDeltaConfig.clear();
		//System.out.println(myDeltaConfig);
		return usedCategories;
	}
}
