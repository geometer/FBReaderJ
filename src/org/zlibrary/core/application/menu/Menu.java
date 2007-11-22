package org.zlibrary.core.application.menu;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.resources.ZLResourceKey;
public class Menu {
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
		// commented while ZLResource is not implemented
		//myItems.add(new Menubar.PlainItem(myResource.getResource(key).value(), actionId));
		myItems.add(new Menubar.PlainItem(key.Name, actionId));
	}
	
	public void addSeparator() {
		myItems.add(new Menubar.Separator());
	}
	
	public Menu addSubmenu(ZLResourceKey key) {
		// commented while ZLResource is not implemented
		//Menubar.Submenu submenu = new Menubar.Submenu(myResource.getResource(key));
		Menubar.Submenu submenu = new Menubar.Submenu(null);
		myItems.add(submenu);
		return submenu;
	}

	public List<Item> items() {
		return Collections.unmodifiableList(myItems);
	}
}
