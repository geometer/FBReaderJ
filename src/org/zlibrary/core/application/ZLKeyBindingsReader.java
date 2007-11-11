package org.zlibrary.core.application;

import java.util.Map;

public class ZLKeyBindingsReader {//extends ZLXMLReader {
	private Map<String,Integer> myKeymap;// = new HashMap<String,Integer>();

	public ZLKeyBindingsReader(Map<String,Integer> keymap) {
		myKeymap = keymap; 
	}

		
	public void startElementHandler(String tag, String[] attributes) {
		final String BINDING = "binding";

		if (BINDING == tag) {
			//const char *key = attributeValue(attributes, "key");
			//const char *action = attributeValue(attributes, "action");
			//if ((key != 0) && (action != 0)) {
			//	myKeymap[key] = atoi(action);
			//}
		}
	}

	public void readBindings() {
		//readDocument(ZLApplication::DefaultFilesPathPrefix() + KeymapFile);
	}
}
