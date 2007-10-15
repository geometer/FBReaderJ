package org.zlibrary.options.impl;

public final class ZLDoubleOption extends ZLOptionImpl{
	
	private double myValue;
	private double myDefaultValue;
	
	public double getValue(){
		return 1.0;
	}
	
	public void setValue(double value){
		
	}
	
	public ZLDoubleOption (String category, String group, String optionName, double defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
	}
	
}
