package org.zlibrary.core.xml;

import org.zlibrary.core.xml.sax.ZLSaxXMLProcessor;

public abstract class ZLXMLProcessor {
	public static ZLXMLProcessor createXMLProcessor() {
		return new ZLSaxXMLProcessor();
	}
	
	public abstract void read(ZLXMLReader reader, String fileName);
}
