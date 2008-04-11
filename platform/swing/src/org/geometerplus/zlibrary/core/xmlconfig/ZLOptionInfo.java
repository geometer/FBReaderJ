package org.geometerplus.zlibrary.core.xmlconfig;

final class ZLOptionInfo {
	private String myValue;
	private String myCategory;
	//private String myName;

	/*
	ZLOptionInfo(String name, String value, String category) {
		myValue = (value != null) ? value : "";
		myCategory = (category != null) ? category : "";
		myName = (name != null) ? name : "";
	}
	*/
	
	ZLOptionInfo(String value, String category) {
		myValue = (value != null) ? value : "";
		myCategory = (category != null) ? category : "";
	}

	void setValue(String value) {
		if (value != null) {
			myValue = value;
		}
	}
	
	void setCategory(String category) {
		if (category != null) {
			myCategory = category;
		}
	}
	
	String getValue() {
		return myValue;
	}

	String getCategory() {
		return myCategory;
	}

	/*
	public String toXML(String name) {
		return "    <option name=\"" + myName + "\" value=\"" + myValue + "\"/>\n";
	}
	*/
	
	public String toXML(String name) {
		return "    <option name=\"" + name + "\" value=\"" + myValue + "\"/>\n";
	}
}
