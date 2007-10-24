package org.zlibrary.options;

public final class ZLDoubleOption extends ZLOption{
	
	private double myValue;
	private double myDefaultValue;
	
	public double getValue(){
		return myValue;
	}
	
	public void setValue(double value){
		//TODO установка значения
	}
	
	public ZLDoubleOption (String category, String group, String optionName, double defaultValue){
		super(category, group, optionName);
		myDefaultValue = defaultValue;
		myValue = myDefaultValue;
	}
	
}
