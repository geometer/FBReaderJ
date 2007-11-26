package org.zlibrary.core.application.menu;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.resources.ZLResourceKey;

public class Menu {
	public interface Item {
	}

	private final List<Item> myItems;
	private final ZLResource myResource;

	protected Menu(ZLResource resource) {
		myItems = new LinkedList<Item>();
		myResource = resource;
	}

	protected ZLResource getResource() {
		return myResource;
	}

	public void addItem(int actionId, ZLResourceKey key) {
		myItems.add(new Menubar.PlainItem(myResource.getResource(key).value(), actionId));
	}
	
	public void addSeparator() {
		myItems.add(new Menubar.Separator());
	}
	
	public Menu addSubmenu(ZLResourceKey key) {
		Menubar.Submenu submenu = new Menubar.Submenu(myResource.getResource(key));
		myItems.add(submenu);
		return submenu;
	}

	List<Item> getItems() {
		return Collections.unmodifiableList(myItems);
	}
}
