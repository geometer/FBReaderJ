package org.zlibrary.core.xml;

public abstract class ZLXMLProcessorFactory {
	private static ZLXMLProcessorFactory ourInstance;

	public static ZLXMLProcessorFactory getInstance() {
		return ourInstance;
	}

	protected ZLXMLProcessorFactory() {
		ourInstance = this;
	}

	public abstract ZLXMLProcessor createXMLProcessor();
}
