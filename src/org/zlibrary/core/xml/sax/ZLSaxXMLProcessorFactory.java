package org.zlibrary.core.xml.sax;

import org.zlibrary.core.xml.ZLXMLProcessorFactory;

public class ZLSaxXMLProcessorFactory extends ZLXMLProcessorFactory {
	protected ZLSaxXMLProcessor createXMLProcessor() {
		return new ZLSaxXMLProcessor();
	}
}
