package org.zlibrary.core.application.menu;

import org.zlibrary.core.resources.ZLResource;

public class Menubar extends Menu {
	public static class PlainItem implements  Item {
		private String myName;
		private int myActionId;

		public  PlainItem() {}
		public PlainItem(String name, int actionId) {
			myName = name;
			myActionId = actionId;
		}

		public String getName() {
			return myName;
		}
		
		public int getActionId() {
			return myActionId;
		}
	};

	//muttiple inheritance!!!
	public static class Submenu extends Menu implements Item {
		public Submenu(ZLResource resource) {
			super(resource);
		}

		public String getMenuName() {
			return getResource().value();
		}
	};
	
	public static class Separator implements Item {
	};
		
	public Menubar() {
		super(ZLResource.resource("menu"));
	}
}
