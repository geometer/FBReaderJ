package org.geometerplus.zlibrary.core.html.own;

import org.geometerplus.zlibrary.core.html.ZLHtmlProcessor;
import org.geometerplus.zlibrary.core.html.ZLHtmlProcessorFactory;

public class ZLOwnHtmlProcessorFactory extends ZLHtmlProcessorFactory {
	public ZLHtmlProcessor createHtmlProcessor() {
		return new ZLOwnHtmlProcessor();
	}
}
