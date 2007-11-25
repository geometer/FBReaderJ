package org.zlibrary.core.options.config;

import java.util.*;

public final class ZLGroup {
	private Map<String, ZLOptionValue> myData;
	
	public ZLGroup (){
		myData = new LinkedHashMap<String, ZLOptionValue>();
	}
	
	public Collection<ZLOptionValue> getValues() {
		return Collections.unmodifiableCollection(myData.values());
	}
	
	public String getValue(String name){
		ZLOptionValue temp = myData.get(name);
		if (temp != null){
			return temp.getValue();
		} else {
			return null;
		}
	}
	
	public void setValue(String name, String value, String category) {
		ZLOptionValue temp = myData.get(name);
		if (temp == null) { 
			myData.put(name, new ZLOptionValue(name, value, category));
		} else {
			temp.setValue(value);
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
