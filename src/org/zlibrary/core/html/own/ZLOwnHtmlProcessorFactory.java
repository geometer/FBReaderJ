package org.zlibrary.core.html.own;

import org.zlibrary.core.html.ZLHtmlProcessor;
import org.zlibrary.core.html.ZLHtmlProcessorFactory;

public class ZLOwnHtmlProcessorFactory extends ZLHtmlProcessorFactory {
	public ZLHtmlProcessor createHtmlProcessor() {
		return new ZLOwnHtmlProcessor();
	}
}
