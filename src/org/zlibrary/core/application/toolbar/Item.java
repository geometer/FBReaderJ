package org.zlibrary.core.application.toolbar;

abstract public class Item {
	public enum Type {
		BUTTON,
		OPTION_ENTRY,
		SEPARATOR
	}

	public Item() {}
	
	public abstract Type getType();
}
