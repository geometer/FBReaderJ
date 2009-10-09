package org.test.zlibrary.options;

import junit.framework.TestCase;

import org.geometerplus.zlibrary.core.xml.own.ZLOwnXMLProcessorFactory;
import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.core.xmlconfig.ZLXMLConfigManager;

abstract class ZLOptionTests extends TestCase {
	ZLOptionTests(String inputPath, String outputPath) {
		new ZLOwnXMLProcessorFactory();
		new ZLXMLConfigManager(inputPath, outputPath);
	}

	ZLOptionTests(String path) {
		this(path, path);
	}

	ZLOptionTests() {
		this(null);
	}

	protected ZLConfig getConfig() {
		return ZLXMLConfigManager.getConfig();
	}
}
