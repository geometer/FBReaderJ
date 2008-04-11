package org.geometerplus.zlibrary.core.xmlconfig;

final class ZLOptionID {
	private String myGroup = "";
	private String myName = "";

	public ZLOptionID(String group, String name) {
		if (group != null) {
			myGroup = group;
		}
		if (name != null) {
			myName = name;
		}
	}

	public String getGroup() {
		return myGroup;
	}

	public String getName() {
		return myName;
	}

	public int hashCode() {
		return myName.length();
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof ZLOptionID)) {
			return false;
		}

		ZLOptionID arg = (ZLOptionID) o;

		if (arg.hashCode() != this.hashCode()) {
			return false;
		}

		return arg.myName.equals(myName) && arg.myGroup.equals(myGroup);

	}
}
