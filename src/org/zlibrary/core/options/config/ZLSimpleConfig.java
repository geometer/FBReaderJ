package org.zlibrary.core.options.config;

import java.util.Map;

/*package*/ interface ZLSimpleConfig {
	
	public void removeGroup(String name);

	public String getValue(String group, String name, String defaultValue);
	
	public void setValue(String group, String name, String value, String category);
	
	public void unsetValue(String group, String name);
	
	public String toString();
	
	public Map<String, ZLGroup> getGroups();
}
