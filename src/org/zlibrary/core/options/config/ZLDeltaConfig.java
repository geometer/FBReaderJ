package org.zlibrary.core.options.config;

import java.util.*;

/*package*/ final class ZLDeltaConfig {

	private final ZLDeletedValuesTree myDeletedValues = new ZLDeletedValuesTree();

	private final Set<String> myDeletedGroups = new HashSet<String>();

	private final ZLSimpleConfig mySetValues = new ZLSimpleConfig();

	public ZLDeltaConfig() {
	}

	public Set<String> getDeletedGroups() {
		return Collections.unmodifiableSet(myDeletedGroups);
	}

	public ZLDeletedValuesTree getDeletedValues() {
		return myDeletedValues;
	}

	public ZLSimpleConfig getSetValues() {
		return mySetValues;
	}

	public String getValue(String group, String name, String defaultValue) {
		return mySetValues.getValue(group, name, defaultValue);
	}

	public void setValue(String group, String name, String value,
			String category) {
		mySetValues.setValue(group, name, value, category);
	}

	public void setCategory(String group, String name, String cat) {
		mySetValues.setCategory(group, name, cat);
	}

	public void unsetValue(String group, String name) {
		// TODO щрн окнун??
		myDeletedValues.add(group, name);
		mySetValues.unsetValue(group, name);
	}

	public void removeGroup(String group) {
		myDeletedGroups.add(group);
		mySetValues.removeGroup(group);
	}

	public void clear() {
		myDeletedValues.clear();
		myDeletedGroups.clear();
		mySetValues.clear();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("<delta>\n  <delete>\n");
		for (String group : myDeletedGroups) {
			sb.append("    <group name=\"" + group + "\"/>\n");
		}

		{
			Set<String> values = myDeletedValues.getGroups();
			for (String group : values) {
				sb.append("    <group name=\"" + group + "\">\n");
				for (String option : myDeletedValues.getOptions(group)) {
					sb.append("      <option name=\"" + option + "\"/>\n");
				}
				sb.append("    </group>\n");
			}
			sb.append("  </delete>\n");
		}

		{
			Set<ZLGroup> groups = mySetValues.getGroups();
			for (ZLGroup group : groups) {
				sb.append("  <group name=\"" + group.getName() + "\">\n");
				for (ZLOptionInfo option : group.getOptions()) {
					sb.append("    <option name=\"" + option.getName() + "\" ");
					sb.append("value=\"" + option.getValue() + "\" ");
					sb.append("category=\"" + option.getCategory() + "\">\n");
				}
				sb.append("  </group>\n");
			}
			sb.append("</delta>");
		}
		return sb.toString();
	}
}
