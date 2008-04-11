package org.geometerplus.zlibrary.core.config;

public abstract class ZLConfigManager {
	private static ZLConfigManager ourInstance;
	private static ZLConfig ourConfig;

	// TODO: remove this method
	public static ZLConfigManager getInstance() {
		return ourInstance;
	}

	public static ZLConfig getConfig() {
		return ourConfig;
	}

	protected static void setConfig(ZLConfig config) {
		ourConfig = config;
	}

	public static void release() {
		if (ourInstance != null) {
			ourInstance.shutdown();
			ourInstance = null;
			ourConfig = null;
		}
	}

	protected abstract void shutdown();

	// TODO: remove these methods
	public abstract void saveAll();
	public abstract void saveDelta();

	protected ZLConfigManager() {
		ourInstance = this;
	}
}
