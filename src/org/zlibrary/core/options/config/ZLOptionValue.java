package org.zlibrary.core.options.config;

public class ZLOptionValue {
	private String myValue = "";
	private String myCategory;
	private String myName = "";
	
	public ZLOptionValue(String name, String value, String category) {
		myValue = value.intern();
		myCategory = category.intern();
		myName = name.intern();
	}
	
	public void setValue(String value) {
		myValue = value.intern();
	}
	
	public String getValue() {
		return myValue;
	}
	
	public String getName() {
		return myName;
	}
	
	public String getCategory() {
		return myCategory;
	}
	
	public String toString() {
		return "    <option name=\"" + myName 
		+ "\" value=\"" + myValue + "\"/>\n";
	}
}
