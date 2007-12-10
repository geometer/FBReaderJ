package org.zlibrary.core.xml.own;

import org.zlibrary.core.xml.ZLXMLProcessorFactory;

public class ZLOwnXMLProcessorFactory extends ZLXMLProcessorFactory {
	protected ZLOwnXMLProcessor createXMLProcessor() {
		return new ZLOwnXMLProcessor();
	}
}
