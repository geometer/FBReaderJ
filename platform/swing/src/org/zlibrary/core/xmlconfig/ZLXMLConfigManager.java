package org.zlibrary.core.xmlconfig;

import org.zlibrary.core.config.*;

public class ZLXMLConfigManager extends ZLConfigManager {
	private String myDirectoryPath;

	// TODO: remove this constructor
	public ZLXMLConfigManager(String inputPath, String outputPath) {
		myDirectoryPath = outputPath;
		ZLConfigImpl config = new ZLConfigImpl();
		if (inputPath != null) {
			new ZLConfigReader(config, inputPath).read();
		}
		setConfig(config);
	}

	public ZLXMLConfigManager(String directoryPath) {
		this(directoryPath, directoryPath);
	}

	protected void shutdown() {
		saveAll();
	}

	public void saveAll() {
		if (myDirectoryPath != null) {
			new ZLConfigWriter((ZLConfigImpl)getConfig(), myDirectoryPath).write();
		}
	}

	public void saveDelta() {
		if (myDirectoryPath != null) {
			new ZLConfigWriter((ZLConfigImpl)getConfig(), myDirectoryPath).writeDelta();
		}
	}
}
