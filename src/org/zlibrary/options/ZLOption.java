package org.zlibrary.options;

public abstract class ZLOption {
	public static final String LOOK_AND_FEEL_CATEGORY = "UI";
	public static final String CONFIG_CATEGORY = "options";
	public static final String STATE_CATEGORY = "state";
	
	public String myCategory;
	public String myGroup;
	public String myOptionName;
	public boolean myIsSynchronized;

	public void clearGroup(String group){
		
	}
	
	//private final ZLOption& operator = (final ZLOptions options);
	
	public ZLOption (String category, String group, String optionName){
		
	}
	//public boolean isAutoSavingSupported();
    //public void startAutoSave(int seconds);
		
	//protected ZLOption(String category, String group, String optionName);

	/*private ZLOption(const ZLOption&);	
	 *private final ZLOption& operator = (const ZLOption&);*/
}
