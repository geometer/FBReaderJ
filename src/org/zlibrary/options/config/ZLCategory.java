package org.zlibrary.options.config;

import java.util.HashMap;
import java.util.Map;

/*package*/ class ZLCategory {
    
    private Map<String, ZLGroup> myData;
    
    public ZLCategory() {
        myData = new HashMap<String, ZLGroup>();
    }
    
    public ZLCategory(Map<String, ZLGroup> map) {
        myData = map;
    }
    
    public void removeGroup(String name) {
        if (myData.get(name) != null){
            myData.remove(name);
        }
    }

    public String getValue(String group, String name, String defaultValue) {
        if (myData.get(group) != null){
            return myData.get(group).getValue(name, defaultValue);
        } else{
            return defaultValue;
        }
    }
    
    public void setValue(String group, String name, String value){
        if (myData.get(group) != null){
            myData.get(group).setValue(name, value);
        } else {
            ZLGroup newgroup = new ZLGroup();
            newgroup.setValue(name, value);
            myData.put(group, newgroup);
        }
    }
    
    public void unsetValue(String group, String name){
        myData.get(group).unsetValue(name);
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        for (String groupName : myData.keySet()){
            sb.append("    " + groupName + "\n" + myData.get(groupName) + "\n");
        }
        return sb.toString();
    }
}
