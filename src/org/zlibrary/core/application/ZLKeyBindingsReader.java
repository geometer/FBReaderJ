package org.zlibrary.core.application;

import java.util.*;
import org.zlibrary.core.util.*;

import org.zlibrary.core.xml.*;

class ZLKeyBindingsReader extends ZLXMLReaderAdapter {
	private HashMap myKeymap;
	
	public ZLKeyBindingsReader(HashMap keymap) {
		myKeymap = keymap; 
	}
		
	public void startElementHandler(String tag, ZLStringMap attributes) {
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
