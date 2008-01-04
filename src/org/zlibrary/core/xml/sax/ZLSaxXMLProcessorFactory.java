package org.zlibrary.core.xml.sax;

import org.zlibrary.core.xml.ZLXMLProcessorFactory;

public class ZLSaxXMLProcessorFactory extends ZLXMLProcessorFactory {
	public ZLSaxXMLProcessor createXMLProcessor() {
		return new ZLSaxXMLProcessor();
	}
}
