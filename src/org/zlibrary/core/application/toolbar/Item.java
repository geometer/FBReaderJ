package org.zlibrary.core.application.toolbar;

public interface Item {
	public enum Type {
		BUTTON,
		OPTION_ENTRY,
		SEPARATOR
	}

	public Type getType();
}
