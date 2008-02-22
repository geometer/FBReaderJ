package org.zlibrary.core.html;

public abstract class ZLHtmlLProcessorFactory {
	private static ZLHtmlLProcessorFactory ourInstance;

	public static ZLHtmlLProcessorFactory getInstance() {
		return ourInstance;
	}

	protected ZLHtmlLProcessorFactory() {
		ourInstance = this;
	}

	public abstract ZLHtmlProcessor createHTMLProcessor();
}
