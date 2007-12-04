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
        //System.out.println(group + " - - - -" + name);
		mySetValues.unsetValue(group, name);
        //System.out.println(mySetValues.getValue(group, name, "NOVALUE"));
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
        StringBuffer sb = new StringBuffer("<config>\n");
        for (String group : myDeletedGroups) {
            sb.append("    <group name=\"" + group + "\"/>\n");
        }

        Set<ZLGroup> setGroups = mySetValues.getGroups();
        Set<String> writtenGroups = new HashSet<String>();

        for (ZLGroup group : setGroups) {
            sb.append("  <group name=\"" + group.getName() + "\">\n");
            writtenGroups.add(group.getName());
            for (ZLOptionInfo option : group.getOptions()) {
                sb.append("    <option name=\"" + option.getName() + "\" ");
                sb.append("value=\"" + option.getValue() + "\" ");
                sb.append("category=\"" + option.getCategory() + "\">\n");
            }
            if (myDeletedValues.getOptions(group.getName()) != null) {
                for (String option : myDeletedValues
                        .getOptions(group.getName())) {
                    sb.append("    <option name=\"" + option + "\"/>\n");
                }
            }
            sb.append("  </group>\n");
        }

        Set<String> values = myDeletedValues.getGroups();
        Set<String> otherGroups = new HashSet<String>(values);
        otherGroups.removeAll(writtenGroups);
        for (String group : otherGroups) {
            sb.append("  <group name=\"" + group + "\">\n");
            for (String option : myDeletedValues.getOptions(group)) {
                sb.append("      <option name=\"" + option + "\"/>\n");
            }
            sb.append("  </group>\n");
        }

        sb.append("</config>");

        return sb.toString();
	}
}
