package org.zlibrary.options.config;

import java.util.*;

/*package*/ class ZLGroup {
	private Map<String, String> myData;
	
	public ZLGroup (){
		myData = new HashMap<String, String>();
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
		myData.put(name, data);
	}
    
    public void unsetValue(String name) {
        myData.remove(name);
    }
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (String name : myData.keySet()){
			sb.append("        " + name + " : " + myData.get(name) + "\n");
		}
		return sb.toString();
	}
}
