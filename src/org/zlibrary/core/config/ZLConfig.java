package org.zlibrary.core.config;

public interface ZLConfig {
	void removeGroup(String name);
	String getValue(String group, String name, String defaultValue);
	void setValue(String group, String name, String value, String category);
	void unsetValue(String group, String name);
}
