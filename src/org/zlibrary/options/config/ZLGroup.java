package org.zlibrary.options.config;

import java.util.*;

/*package*/ class ZLGroup {
	private Map<String, String> myData;
	
	public ZLGroup (){
		myData = new LinkedHashMap<String, String>();
	}
	
	public String getValue(String name, String defaultValue){
        String temp = myData.get(name);
        if (temp != null){
            return temp;
        } else {
            return defaultValue;
        }
	}
	
	public void setValue(String name, String data) {
        if (myData.get(name) == null) { 
            myData.put(name, new String(data));
        } else {
            myData.put(name, data);
        }
	}
    
    public void unsetValue(String name) {
        myData.remove(name);
    }
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (String name : myData.keySet()){
			sb.append("    <option name=\"" + name 
                       + "\" value=\"" + myData.get(name) + "\"/>\n");
		}
		return sb.toString();
	}
}
