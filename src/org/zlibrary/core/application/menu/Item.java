package org.zlibrary.core.application.menu;

public interface Item {
	public enum ItemType {
			ITEM,
			SUBMENU,
			SEPARATOR
	};

	public ItemType type();

	/*
	private ItemType myType;
		
	public ItemType type() {
		return myType;
	}

	protected Item(ItemType type) {
		myType = type;
	}*/
}



