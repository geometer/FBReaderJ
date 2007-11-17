package org.zlibrary.core.library;

import org.zlibrary.core.view.ZLPaintContext;

public abstract class ZLibrary {
	public static ZLibrary getInstance() {
		return ourImplementation;
	}

	private static ZLibrary ourImplementation;

	protected ZLibrary() {
		ourImplementation = this;
	}

	abstract public ZLPaintContext createPaintContext();
	abstract public String getApplicationName();
}
