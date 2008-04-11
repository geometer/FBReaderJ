package org.geometerplus.zlibrary.core.html;

public abstract class ZLHtmlProcessorFactory {
	private static ZLHtmlProcessorFactory ourInstance;

	public static ZLHtmlProcessorFactory getInstance() {
		return ourInstance;
	}

	protected ZLHtmlProcessorFactory() {
		ourInstance = this;
	}

	public abstract ZLHtmlProcessor createHtmlProcessor();
}
