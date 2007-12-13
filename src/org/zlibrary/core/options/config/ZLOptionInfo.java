package org.zlibrary.core.options.config;

final class ZLOptionInfo {
	private String myValue;
	private String myCategory;
	private String myName;

	public ZLOptionInfo(String name, String value, String category) {
		myValue = (value != null) ? value : "";
		myCategory = (category != null) ? category : "";
		myName = (name != null) ? name : "";
	}

	public void setValue(String value) {
		if (value != null) {
			myValue = value;
		}
	}
	
	public void setCategory(String category) {
		if (category != null) {
			myCategory = category;
		}
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

	public int hashCode() {
		return myName.hashCode();
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof ZLOptionInfo)) {
			return false;
		}

		ZLOptionInfo arg = (ZLOptionInfo) o;

		if (arg.hashCode() != hashCode()) {
			return false;
		}

		return arg.myName.equals(myName);
	}

	public String toString() {
		return "    <option name=\"" + myName + "\" value=\"" + myValue
				+ "\"/>\n";
	}
}
