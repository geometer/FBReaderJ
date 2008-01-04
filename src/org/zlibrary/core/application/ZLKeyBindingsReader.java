package org.zlibrary.core.application;

import java.util.HashMap;

import org.zlibrary.core.xml.ZLXMLReaderAdapter;

class ZLKeyBindingsReader extends ZLXMLReaderAdapter {
	private HashMap<String,Integer> myKeymap;
	
	public ZLKeyBindingsReader(HashMap<String,Integer> keymap) {
		myKeymap = keymap; 
	}
		
	public void startElementHandler(String tag, StringMap attributes) {
		if ("binding".equals(tag)) {
			String key = attributes.getValue("key");
			String action = attributes.getValue("action");
			if ((key != null) && (action != null)) {
				try {
					int actionId = Integer.parseInt(action);
					myKeymap.put(key, actionId);
				} catch (NumberFormatException e) {
				}
			}
		}
	}

	public void readBindings() {
		read("data/default/keymap.xml");
	}
}
