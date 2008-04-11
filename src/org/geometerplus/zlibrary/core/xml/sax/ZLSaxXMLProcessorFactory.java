package org.geometerplus.zlibrary.core.xml.sax;

import org.geometerplus.zlibrary.core.xml.ZLXMLProcessorFactory;

public class ZLSaxXMLProcessorFactory extends ZLXMLProcessorFactory {
	public ZLSaxXMLProcessor createXMLProcessor() {
		return new ZLSaxXMLProcessor();
	}
}
