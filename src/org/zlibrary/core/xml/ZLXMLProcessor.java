package org.zlibrary.core.xml;

import java.io.InputStream;

import org.zlibrary.core.library.ZLibrary;

public abstract class ZLXMLProcessor {
	public abstract boolean read(ZLXMLReader xmlReader, InputStream stream);

	public boolean read(ZLXMLReader xmlReader, String fileName) {
		InputStream stream = ZLibrary.getInstance().getInputStream(fileName);
		return (stream != null) ? read(xmlReader, stream) : false;
	}
}
