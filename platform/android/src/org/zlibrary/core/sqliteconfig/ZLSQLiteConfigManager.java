package org.zlibrary.core.sqliteconfig;

import org.zlibrary.core.config.ZLConfig;
import org.zlibrary.core.config.ZLConfigManager;

public class ZLSQLiteConfigManager extends ZLConfigManager {
	public ZLSQLiteConfigManager() {
		setConfig(new ZLSQLiteConfig());
	}

	public void shutdown() {
		saveAll();
	}

	public void saveAll() {
	}

	public void saveDelta() {
	}
}
