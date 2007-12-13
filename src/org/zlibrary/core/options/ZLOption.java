package org.zlibrary.core.options;

import org.zlibrary.core.options.config.ZLConfig;
import org.zlibrary.core.options.config.ZLConfigInstance;

public abstract class ZLOption {
	public static final String LOOK_AND_FEEL_CATEGORY = "ui";
	public static final String CONFIG_CATEGORY = "options";
	public static final String STATE_CATEGORY = "state";

	protected final ZLConfig myConfig = ZLConfigInstance.getInstance();
	protected final String myCategory;
	protected final String myGroup;
	protected final String myOptionName;
	protected boolean myIsSynchronized;

	/**
	 * конструктор
	 * 
	 * @param config
	 * @param category
	 * @param group
	 * @param optionName
	 */
	protected ZLOption(String category, String group, String name) {
		myCategory = category.intern();
		myGroup = group.intern();
		myOptionName = name.intern();
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
}
