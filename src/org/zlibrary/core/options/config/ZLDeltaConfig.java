package org.zlibrary.core.options.config;

import java.util.*;

/*package*/ final class ZLDeltaConfig {
	
	private ZLSimpleConfigImpl myDeletedValues = new ZLSimpleConfigImpl();
	private Set<String> myDeletedGroups = new HashSet<String>();
	private ZLSimpleConfigImpl mySetValues = new ZLSimpleConfigImpl();
	
	public ZLDeltaConfig() {
	}
	
	public Set<String> getDeletedGroups() {
		return Collections.unmodifiableSet(myDeletedGroups);
	}
	
	public ZLSimpleConfig getDeletedValues() {
		return myDeletedValues;
	}
	
	public ZLSimpleConfigImpl getSetValues() {
		return mySetValues;
	}
	
	public String getValue(String group, String name, String defaultValue) {
		return mySetValues.getValue(group, name, defaultValue);
	}
	
	public void setValue(String group, String name, String value, String category) {
		mySetValues.setValue(group, name, value, category);
	}
    
    public void setCategory(String group, String name, String cat) {
        mySetValues.setCategory(group, name, cat);
    }
	
	public void unsetValue(String group, String name) {
        //TODO щрн окнун??
		myDeletedValues.setValue(group, name, null, null);
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

        Map<String, ZLGroup> values = myDeletedValues.getGroups();
        for (String group : values.keySet()) {
            sb.append("    <group name=\"" + group + "\">\n");
            for (ZLOptionValue option : values.get(group).getValues()) {
                sb.append("      <option name=\"" + option.getName() + "\"/>\n");
            }
            sb.append("    </group>\n");
        }
        sb.append("  </delete>\n");
        
        values = mySetValues.getGroups();
        for (String group : values.keySet()) {
            sb.append("  <group name=\"" + group + "\">\n");
            for (ZLOptionValue option : values.get(group).getValues()) {
                sb.append("    <option name=\"" + option.getName() + "\" ");
                sb.append("value=\"" + option.getValue() + "\" ");
                sb.append("category=\"" + option.getCategory() + "\">\n");
            }
            sb.append("  </group>\n");
        }
        sb.append("</delta>");
        return sb.toString();
    }
}
