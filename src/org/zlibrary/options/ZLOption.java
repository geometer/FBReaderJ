package org.zlibrary.options;

public abstract class ZLOption {
	public static final String LOOK_AND_FEEL_CATEGORY = "UI";
	public static final String CONFIG_CATEGORY = "options";
	public static final String STATE_CATEGORY = "state";
	
	protected String myCategory;
	protected String myGroup;
	protected String myOptionName;
	//protected boolean myIsSynchronized;
	
	/**
	 * чтобы не забыть определить этот метод во всех final наследниках. 
	 * установка значения в дефолт. полезный метод.
	 */
	public abstract void setValueToDefault();
	
	/**
	 * конструктор.
	 * @param category
	 * @param group
	 * @param optionName
	 */
	//TODO когда разберусь написать сюда джавадок
	public ZLOption (String category, String group, String optionName){
		myCategory = category;
		myGroup = group;
		myOptionName = optionName;
	}
	
	//public void clearGroup(String group){}
	//private final ZLOption& operator = (final ZLOptions options);
	//public boolean isAutoSavingSupported();
    //public void startAutoSave(int seconds);
	//protected ZLOption(String category, String group, String optionName);
	//private ZLOption(const ZLOption&);	
	//private final ZLOption& operator = (const ZLOption&);
}
