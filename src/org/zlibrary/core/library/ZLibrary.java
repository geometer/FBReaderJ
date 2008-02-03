package org.zlibrary.core.library;

import java.io.InputStream;
import java.util.*;
import org.zlibrary.core.util.*;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.core.xml.ZLStringMap;
import org.zlibrary.core.xml.ZLXMLReaderAdapter;

public abstract class ZLibrary {
	public static final String JAR_DATA_PREFIX = "#JAR#://";
	public static  String PathDelimiter;
	public static String FileNameDelimiter;
	private final HashMap myProperties = new HashMap();

	public static ZLibrary getInstance() {
		return ourImplementation;
	}
		
	private static ZLibrary ourImplementation;

	protected ZLibrary() {
		ourImplementation = this;
	}

	public final String getApplicationName() {
		return (String)myProperties.get("applicationName");
	}

	protected final Class getApplicationClass() {
		try {
			Class clazz = Class.forName((String)myProperties.get("applicationClass"));
			if ((clazz != null) && ZLApplication.class.isAssignableFrom(clazz)) {
				return clazz;
			}
		} catch (ClassNotFoundException e) {
		}
		return null;
	}

	public final InputStream getInputStream(String fileName) {
		if (fileName.startsWith(JAR_DATA_PREFIX)) {
			return getResourceInputStream(fileName.substring(JAR_DATA_PREFIX.length()));
		} else {
			return getFileInputStream(fileName);
		}
	}

	abstract protected InputStream getResourceInputStream(String fileName);
	abstract protected InputStream getFileInputStream(String fileName);

	abstract public ZLPaintContext createPaintContext();
	abstract public void openInBrowser(String reference);

	protected final void loadProperties() {
		new ZLXMLReaderAdapter() {
			public void startElementHandler(String tag, ZLStringMap attributes) {
				if (tag.equals("property")) {
					myProperties.put(attributes.getValue("name"), attributes.getValue("value"));
				}
			}
		}.read(JAR_DATA_PREFIX + "data/application.xml");
	}
}
