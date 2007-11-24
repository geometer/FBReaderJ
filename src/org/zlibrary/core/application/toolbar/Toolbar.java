package org.zlibrary.core.application.toolbar;

import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.resources.ZLResourceKey;

public class Toolbar {
	private final List<Item> myItems;
	private final ZLResource myResource;

	public Toolbar() {
		myItems = new LinkedList<Item>();
		myResource = ZLResource.resource("toolbar");
	}
	
	public void addButton(int actionId, ZLResourceKey key) {
		addButton(actionId, key, null);
	}

	public void addButton(int actionId, ZLResourceKey key, ButtonGroup group) {
		ButtonItem button = new ButtonItem(actionId, key.Name, myResource.getResource(key));
		myItems.add(button);
		button.setButtonGroup(group);
	}
	
	public ButtonGroup createButtonGroup(int unselectAllButtonsActionId) {
		return new ButtonGroup(unselectAllButtonsActionId);
	}
	
	/*public void addOptionEntry(ZLOptionEntry entry) {
		if (entry != null) {
			myItems.add(new OptionEntryItem(entry));
		}
	}*/
	
	public void addSeparator() {
		myItems.add(new SeparatorItem());
	}

	public List<Item> items() {
		return Collections.unmodifiableList(myItems);
	}
}
