package org.zlibrary.options.config;

import java.util.*;

public class ZLConfig {
	// public abstract void unsetValue(String group, String name);

	// public abstract  boolean isAutoSavingSupported() const = 0;
	// public abstract  void startAutoSave(int seconds) = 0;
	private Map<String, ZLGroup> myData;
	
	public ZLConfig (){
		myData = new HashMap<String, ZLGroup>();
	}
	
	public ZLConfig (Map<String, ZLGroup> map){
		myData = map;
	}
	
	public void removeGroup(String name){
		if (myData.get(name) != null){
			myData.remove(name);
		}
	}

	public String getValue(String group, String name){
		if (myData.get(group) != null){
			return myData.get(group).getValue(name);
		} else{
			return null;
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
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for (String groupName : myData.keySet()){
			sb.append("" + groupName + "\n" + myData.get(groupName) + "\n");
		}
		return sb.toString();
	}
}
