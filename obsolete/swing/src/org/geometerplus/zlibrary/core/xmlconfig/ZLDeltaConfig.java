/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.core.xmlconfig;

import java.util.*;

final class ZLDeltaConfig {
	private final ZLDeletedValuesSet myDeletedValues = new ZLDeletedValuesSet();
	private final HashSet<String> myDeletedGroups = new HashSet<String>();
	private final ZLSimpleConfig myChangedValues = new ZLSimpleConfig();

	public Set<String> getDeletedGroups() {
		return Collections.unmodifiableSet(myDeletedGroups);
	}

	public ZLDeletedValuesSet getDeletedValues() {
		return myDeletedValues;
	}

	public ZLSimpleConfig changedValues() {
		return myChangedValues;
	}

	/**
	 * 
	 * @param group
	 * @param name
	 * @param defaultValue
	 * @return defaultValue - when this value is not set or deleted
	 * new value (from changedValues) - when it was set
	 * null - when it was deleted
	 */
	public String getValue(String group, String name, String defaultValue) {
		String value = myChangedValues.getValue(group, name, defaultValue);
		if ((value == null) || (value.equals(defaultValue))) {
			if (myDeletedValues.contains(group, name)) {
				return null;
			} else {
				return defaultValue;
			}
		} else {
			return value;
		}
	}

	public void setValue(String group, String name, String value,
			String category) {
		myChangedValues.setValue(group, name, value, category);
	}
/*
	public void setCategory(String group, String name, String cat) {
		myChangedValues.setCategory(group, name, cat);
	}*/

	public void unsetValue(String group, String name) {
		// TODO щрн окнун??
		myDeletedValues.add(group, name);
		//System.out.println(group + " - - - -" + name);
		//System.out.println(myDeletedValues.getAll());
		myChangedValues.unsetValue(group, name);
		//System.out.println(myChangedValues.getValue(group, name, "NOVALUE"));
	}

	public void removeGroup(String group) {
		myDeletedGroups.add(group);
		myChangedValues.removeGroup(group);
	}

	public void clear() {
		myDeletedValues.clear();
		myDeletedGroups.clear();
		myChangedValues.clear();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("<config>\n");
		for (String group : myDeletedGroups) {
			sb.append("  <group name=\"" + group + "\"/>\n");
		}

		for (String group : myDeletedValues.getGroups()) {
			sb.append("  <group name=\"" + group + "\">\n");
			for (ZLOptionID option : myDeletedValues.getAll()) {
				if (option.getGroup().equals(group)) {
					sb.append("    <option name=\"" + option.getName() + "\"/>\n");
				}
			}
			sb.append("  </group>\n");
		}

		//Set<String> writtenGroups = new HashSet<String>();

		for (String groupName : myChangedValues.groupNames()) {
			sb.append("  <group name=\"" + groupName + "\">\n");
			//writtenGroups.add(group.getName());
			ZLGroup group = myChangedValues.getGroup(groupName);
			for (String optionName : group.optionNames()) {
				ZLOptionInfo option = group.getOption(optionName);
				sb.append("    <option name=\"" + optionName + "\" ");
				sb.append("value=\"" + option.getValue() + "\" ");
				sb.append("category=\"" + option.getCategory() + "\"/>\n");
			}
			for (ZLOptionID option : myDeletedValues.getAll()) {
				if (option.getGroup().equals(group)) {
					sb.append("    <option name=\"" + option.getName() + "\"/>\n");
				}
			}
			sb.append("  </group>\n");
		}
		
		sb.append("</config>");

		return sb.toString();
	}
}
