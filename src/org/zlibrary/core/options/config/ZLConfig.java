package org.zlibrary.core.options.config;

public interface ZLConfig {

	public void removeGroup(String name);

	public String getValue(String group, String name, String defaultValue);

	public void setValue(String group, String name, String value,
			String category);

	public void unsetValue(String group, String name);
}
