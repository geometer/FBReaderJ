package org.zlibrary.core.html.own;

import org.zlibrary.core.html.ZLHtmlProcessor;
import org.zlibrary.core.html.ZLHtmlLProcessorFactory;

public class ZLOwnHtmlProcessorFactory extends ZLHtmlLProcessorFactory {
	public ZLHtmlProcessor createHTMLProcessor() {
		return new ZLOwnHtmlProcessor();
	}
}
