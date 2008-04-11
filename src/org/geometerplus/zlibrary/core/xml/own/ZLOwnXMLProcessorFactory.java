package org.geometerplus.zlibrary.core.xml.own;

import org.geometerplus.zlibrary.core.xml.ZLXMLProcessor;
import org.geometerplus.zlibrary.core.xml.ZLXMLProcessorFactory;

public class ZLOwnXMLProcessorFactory extends ZLXMLProcessorFactory {
	public ZLXMLProcessor createXMLProcessor() {
		return new ZLOwnXMLProcessor();
	}
}
