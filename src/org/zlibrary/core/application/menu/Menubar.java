package org.zlibrary.core.application.menu;

import org.zlibrary.core.resources.ZLResource;

public class Menubar extends Menu {
	class PlainItem extends  Item {
	    private String myName;
	    private int myActionId;

	    public PlainItem(String name, int actionId) {
	    	super(ItemType.ITEM);
	    	myName = name;
	    	myActionId = actionId;
	    }

	    public String name() {
	    	return myName;
	    }
	    public int actionId() {
	    	return myActionId;
	    }
	};
	//muttiple enheritance!!!
	/*class Submenu extends Item, Menu {

			Submenu(const ZLResource &resource);

			const std::string &menuName() const;
	};*/
	class Separator extends Item {

		public Separator() {
			super(ItemType.SEPARATOR);
		}
	};
		
	public Menubar() {
		super(ZLResource.resource("menu"));
	}

}
