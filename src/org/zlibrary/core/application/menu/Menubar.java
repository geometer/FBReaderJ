package org.zlibrary.core.application.menu;

import org.zlibrary.core.resources.ZLResource;

public class Menubar extends Menu {
	public static class PlainItem implements  Item {
		private String myName;
		private int myActionId;

		public  PlainItem() {}
		public PlainItem(String name, int actionId) {
			//super(ItemType.ITEM);
			myName = name;
			myActionId = actionId;
		}

		public String name() {
			return myName;
		}
		
		public int actionId() {
			return myActionId;
		}
		
		public ItemType type() {
			return ItemType.ITEM;
		}
	};

	//muttiple inheritance!!!
	public static class Submenu extends Menu implements Item {
		public Submenu(ZLResource resource) {
			super(resource);
		}

		public String menuName() {
			return getResource().value();
		}
		
		public ItemType type() {
			return ItemType.SUBMENU;
		}
	};
	
	public static class Separator implements Item {

		public Separator() {
			//super(ItemType.SEPARATOR);
		}
		
		public ItemType type() {
			return ItemType.SEPARATOR;
		}

	};
		
	public Menubar() {
		super(ZLResource.resource("menu"));
	}
}
