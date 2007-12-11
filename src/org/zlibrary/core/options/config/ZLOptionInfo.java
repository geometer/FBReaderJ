package org.zlibrary.core.options.config;

final class ZLOptionInfo {
	private static final String EMPTY_STRING = "";
	private String myValue;
	private String myCategory;
	private String myName;

	public ZLOptionInfo(String name, String value, String category) {
		myValue = (value != null) ? value.intern() : EMPTY_STRING;
		myCategory = (category != null) ? category.intern() : EMPTY_STRING;
		myName = (name != null) ? name.intern() : EMPTY_STRING;
	}

	public void setValue(String value) {
		if ((value != null) && (!myValue.equals(value))) {
			myValue = value.intern();
		}
	}
	
	public void setCategory(String cat) {
		if ((cat != null) && (!myCategory.equals(cat))) {
			myCategory = cat.intern();
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
