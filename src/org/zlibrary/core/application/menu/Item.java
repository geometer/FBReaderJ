package org.zlibrary.core.application.menu;

public class Item {
	public enum ItemType {
			ITEM,
			SUBMENU,
			SEPARATOR
	};
	
	private ItemType myType;
		
	public ItemType type() {
		return myType;
	}

	protected Item(ItemType type) {
		myType = type;
	}
}



