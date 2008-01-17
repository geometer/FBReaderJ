package org.zlibrary.core.application;

import java.util.*;
import org.zlibrary.core.util.*;

import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.xml.*;

class ZLKeyBindingsReader extends ZLXMLReaderAdapter {
	private HashMap myKeymap;
	
	public ZLKeyBindingsReader(HashMap keymap) {
		myKeymap = keymap; 
	}
		
	public void startElementHandler(String tag, ZLStringMap attributes) {
		if ("binding".equals(tag)) {
			String key = attributes.getValue("key");
			String actionId = attributes.getValue("action");
			if ((key != null) && (actionId != null)) {
				myKeymap.put(key, actionId);
			}
		}
	}

	public void readBindings() {
		read(ZLibrary.JAR_DATA_PREFIX + "data/default/keymap.xml");
	}
}
