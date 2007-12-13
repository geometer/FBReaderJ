package org.zlibrary.core.options.config;

public final class ZLConfigInstance {
	private static final ZLConfigImpl myConfig = new ZLConfigImpl();

	public static ZLConfig getInstance() {
		return myConfig;
	}
}
