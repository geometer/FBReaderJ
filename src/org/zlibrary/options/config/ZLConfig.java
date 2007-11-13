package org.zlibrary.options.config;

import java.util.Map;

public interface ZLConfig {
    
    public void removeGroup(String category, String name);

    public String getValue(String category, String group, String name, String defaultValue);
    
    public void setValue(String category, String group, String name, String value);
    
    public void unsetValue(String category, String group, String name);
    
    public String toString();
    
    public Map<String, ZLCategory> getCategories();
}
