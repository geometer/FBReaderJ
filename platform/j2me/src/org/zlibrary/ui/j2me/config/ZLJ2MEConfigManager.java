package org.geometerplus.zlibrary.ui.j2me.config;

import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.core.config.ZLConfigManager;

public class ZLJ2MEConfigManager extends ZLConfigManager {
	public ZLJ2MEConfigManager() {
		setConfig(new ZLJ2MEConfig());
	}

	public void shutdown() {
		saveAll();
	}

	public void saveAll() {
	}

	public void saveDelta() {
	}
}
