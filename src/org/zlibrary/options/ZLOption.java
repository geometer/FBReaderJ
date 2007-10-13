package org.zlibrary.options;

public interface ZLOption {
	public static final String LOOK_AND_FEEL_CATEGORY = "UI";
	public static final String CONFIG_CATEGORY = "options";
	public static final String STATE_CATEGORY = "state";
	
	public void clearGroup(String group);
	//public boolean isAutoSavingSupported();
	//public void startAutoSave(int seconds);
		
	//protected ZLOption(String category, String group, String optionName);

	/*private ZLOption(const ZLOption&);	
	 *private final ZLOption& operator = (const ZLOption&);*/
}
