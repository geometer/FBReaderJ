package org.zlibrary.core.application;

import java.util.HashMap;
import java.util.Map;

import org.zlibrary.core.xml.ZLXMLReader;

class ZLKeyBindingsReader extends ZLXMLReader {
	private Map<String,Integer> myKeymap = new HashMap<String,Integer>();
	private final static String KeymapFile = "data/default/keymap.xml";
	
	public ZLKeyBindingsReader(Map<String,Integer> keymap) {
		myKeymap = keymap; 
	}
		
	public void startElementHandler(String tag, Map<String, String> attributes) {
		if ("binding".equals(tag)) {
			String key = attributes.get("key");
			String action = attributes.get("action");
			if ((key != null) && (action != null)) {
				myKeymap.put(key, Integer.parseInt(action));
			}
		}
	}

	public void readBindings() {
		read(KeymapFile);
	}
}
