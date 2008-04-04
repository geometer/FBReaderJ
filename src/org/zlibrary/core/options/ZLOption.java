package org.zlibrary.core.options;

import org.zlibrary.core.config.ZLConfig;
import org.zlibrary.core.config.ZLConfigManager;

public abstract class ZLOption {
	public static final String LOOK_AND_FEEL_CATEGORY = "ui";
	public static final String CONFIG_CATEGORY = "options";
	public static final String STATE_CATEGORY = "state";
	public static final String EMPTY = "";
	public static final String PLATFORM_GROUP = "PlatformOptions";
	
	private final String myCategory;
	private final String myGroup;
	private String myOptionName;
	protected boolean myIsSynchronized;

	/**
	 * конструктор
	 * 
	 * @param config
	 * @param category
	 * @param group
	 * @param optionName
	 */
	protected ZLOption(String category, String group, String optionName) {
		myCategory = category.intern();
		myGroup = group.intern();
		myOptionName = optionName.intern();
		myIsSynchronized = false;

		/*
		 * String value = myConfig.getValue(group, name, null);
		 * 
		 * 
		 * 
		 * 
		 * if ((value != null)) { && (КАТЕГОРИИ НЕ РАВНЫ)
		 * myConfig.setValue(group, name, value, category); }
		 */
	}

	protected void changeName(String optionName) {
		myOptionName = optionName.intern();
		myIsSynchronized = false;
	}

	/**
	 * @return имя опции. вероятно нужно будет во view
	 */
	public String getName() {
		return myOptionName;
	}

	/**
	 * @return "группу" опции. вероятно нужно будет во view
	 */
	public String getGroup() {
		return myGroup;
	}

	/**
	 * @return "категорию" опции. вероятно нужно будет во view
	 */
	public String getCategory() {
		return myCategory;
	}

	protected final String getConfigValue(String defaultValue) {
		ZLConfig config = ZLConfigManager.getConfig();
		return (config != null) ?
			config.getValue(myGroup, myOptionName, defaultValue) : defaultValue;
	}

	protected final void setConfigValue(String value) {
		ZLConfig config = ZLConfigManager.getConfig();
		if (config != null) {
			config.setValue(myGroup, myOptionName, value, myCategory);
		}
	}

	protected final void unsetConfigValue() {
		ZLConfig config = ZLConfigManager.getConfig();
		if (config != null) {
			config.unsetValue(myGroup, myOptionName);
		}
	}
}
