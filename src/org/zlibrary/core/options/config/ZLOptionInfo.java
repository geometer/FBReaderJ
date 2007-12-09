package org.zlibrary.core.options.config;

/*package*/final class ZLOptionInfo {
	
	private String myValue = "";
	private String myCategory = "";
	private String myName = "";

	public ZLOptionInfo(String name, String value, String category) {
		if (value != null) {
			myValue = value.intern();
		}
		if (category != null) {
			myCategory = category.intern();
		}
		if (name != null) {
			myName = name.intern();
		}
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
		return myName.length();
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof ZLOptionInfo)) {
			return false;
		}

		ZLOptionInfo arg = (ZLOptionInfo) o;

		if (arg.hashCode() != this.hashCode()) {
			return false;
		}

		return arg.myName.equals(myName);

	}

	public String toString() {
		return "    <option name=\"" + myName + "\" value=\"" + myValue
				+ "\"/>\n";
	}
}
