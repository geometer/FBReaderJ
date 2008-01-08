package org.zlibrary.core.xml.own;

import org.zlibrary.core.xml.ZLXMLProcessor;
import org.zlibrary.core.xml.ZLXMLProcessorFactory;

public class ZLOwnXMLProcessorFactory extends ZLXMLProcessorFactory {
	public ZLXMLProcessor createXMLProcessor() {
		return new ZLOwnXMLProcessor();
	}
}
