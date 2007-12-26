package org.zlibrary.core.library;

import java.io.InputStream;

import java.util.Map;
import java.util.TreeMap;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.xml.ZLXMLReader;
import org.zlibrary.core.view.ZLPaintContext;

public abstract class ZLibrary {
	private final TreeMap<String,String> myProperties = new TreeMap<String,String>();

	public static ZLibrary getInstance() {
		return ourImplementation;
	}
	
	private static ZLibrary ourImplementation;

	protected ZLibrary() {
		ourImplementation = this;
	}

	public final String getApplicationName() {
		return myProperties.get("applicationName");
	}

	protected final Class getApplicationClass() {
		try {
			Class clazz = Class.forName(myProperties.get("applicationClass"));
			if ((clazz != null) && ZLApplication.class.isAssignableFrom(clazz)) {
				return clazz;
			}
		} catch (ClassNotFoundException e) {
		}
		return null;
	}

	abstract public ZLPaintContext createPaintContext();
	abstract public InputStream getResourceInputStream(String fileName);

	protected final void loadProperties() {
		new ZLXMLReader() {
			public void startElementHandler(String tag, Map<String, String> attributes) {
				if (tag.equals("property")) {
					myProperties.put(attributes.get("name"), attributes.get("value"));
				}
			}
		}.read("data/application.xml");
	}
}
