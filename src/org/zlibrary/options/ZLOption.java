package org.zlibrary.options;

public abstract class ZLOption {
	public static final String LOOK_AND_FEEL_CATEGORY = "ui";
	public static final String CONFIG_CATEGORY = "options";
	public static final String STATE_CATEGORY = "state";
	
    protected ZLConfig myConfig;
	protected String myCategory;
	protected String myGroup;
	protected String myOptionName;
	protected boolean myIsSynchronized;
	
	
	/**
     * конструктор
     * @param config
     * @param category
     * @param group
     * @param optionName
	 */
	protected ZLOption (ZLConfig config, String category, String group, String optionName){
		myCategory = category;
		myGroup = group;
		myOptionName = optionName;
        myConfig = config;
	}
	
	/**
	 * @return имя опции. вероятно нужно будет во view
	 */
	public String getName(){
		return myOptionName;
	}
	
	/**
	 * @return "группу" опции. вероятно нужно будет во view
	 */
	public String getGroup(){
		return myGroup;
	}
	
	/**
	 * @return "категорию" опции. вероятно нужно будет во view
	 */
	public String getCategory(){
		return myCategory;
	}
	
	/**
	 * проверяем две опции на равенство
	 */
	public boolean equals(Object o){
		if (o == this) 
			return true;
		
		if (! (o.getClass() == this.getClass()))
			return false;
		
		ZLOption op = (ZLOption) o;
			
		return ((op.myOptionName == this.myOptionName) && 
				(op.myCategory == this.myCategory) &&
				(op.myGroup == this.myGroup));
			
	}
    
  //  public abstract String getStringValue();
    
	//public void clearGroup(String group){}
	//private final ZLOption& operator = (final ZLOptions options);
	//public boolean isAutoSavingSupported();
    //public void startAutoSave(int seconds);
	//private ZLOption(const ZLOption&);	
	//private final ZLOption& operator = (const ZLOption&);
}
