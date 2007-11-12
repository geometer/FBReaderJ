package org.zlibrary.core.application.menu;

import java.util.Collections;
import java.util.List;

import org.zlibrary.core.application.menu.Item.ItemType;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.resources.ZLResourceKey;

public class Menu {
    protected ZLResource myResource;    
    private List<Item> myItems;

	public void addItem(int actionId, ZLResourceKey key) {
		//myItems.add(new Menubar.PlainItem(myResource.getResource(key).value(), actionId));
	}
	
	public void addSeparator() {
		//myItems.push_back(new Menubar.Separator());
	}
	
	public Menu addSubmenu(ZLResourceKey key) {
		//Menubar.Submenu submenu = new Menubar.Submenu(myResource.getResource(key));
		//myItems.add(submenu);
		return null;//submenu;
	}

	public List<Item> items() {
		return Collections.unmodifiableList(myItems);
	};

    protected Menu(ZLResource resource) {
    	this.myResource = resource;
    }
}


