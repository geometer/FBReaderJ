package org.zlibrary.core.application.menu;

public interface Item {
	public enum ItemType {
			ITEM,
			SUBMENU,
			SEPARATOR
	};

	public ItemType type();
}



