package org.zlibrary.options.config;

import java.util.*;

/*package*/ public class ZLGroup {
	private Map<String, String> myData;
	
	public ZLGroup (){
		myData = new HashMap<String, String>();
	}
	
	public String getValue(String name){
		return myData.get(name);
	}
	
	public void setValue (String name, String data){
		myData.put(name, data);
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for (String name:myData.keySet()){
			sb.append("    " + name + " : " + myData.get(name) + "\n");
		}
		return sb.toString();
	}
}
