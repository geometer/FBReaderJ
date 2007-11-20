package org.zlibrary.core.xml;

public abstract class ZLXMLProcessorFactory {
	private static ZLXMLProcessorFactory ourInstance;

	static ZLXMLProcessorFactory getInstance() {
		return ourInstance;
	}

	protected ZLXMLProcessorFactory() {
		ourInstance = this;
	}

	protected abstract ZLXMLProcessor createXMLProcessor();
}
