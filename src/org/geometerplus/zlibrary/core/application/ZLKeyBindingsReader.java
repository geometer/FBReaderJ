package org.geometerplus.zlibrary.core.application;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

class ZLKeyBindingsReader extends ZLXMLReaderAdapter {
	private HashMap myKeymap;
	
	public ZLKeyBindingsReader(HashMap keymap) {
		myKeymap = keymap; 
	}
		
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		if ("binding".equals(tag)) {
			String key = attributes.getValue("key");
			String actionId = attributes.getValue("action");
			if ((key != null) && (actionId != null)) {
				myKeymap.put(key, actionId);
			}
		}
		return false;
	}

	public void readBindings() {
		read(ZLibrary.JAR_DATA_PREFIX + "data/default/keymap.xml");
	}
}
