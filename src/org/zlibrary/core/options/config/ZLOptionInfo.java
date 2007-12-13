package org.zlibrary.core.options.config;

final class ZLOptionInfo {
	private String myValue;
	private String myCategory;
	private String myName;

	ZLOptionInfo(String name, String value, String category) {
		myValue = (value != null) ? value : "";
		myCategory = (category != null) ? category : "";
		myName = (name != null) ? name : "";
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

	public String toString() {
		return "    <option name=\"" + myName + "\" value=\"" + myValue + "\"/>\n";
	}
}
