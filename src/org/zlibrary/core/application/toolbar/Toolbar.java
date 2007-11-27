package org.zlibrary.core.application.toolbar;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;

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
	
	public interface Item {
	}
	
	public class ButtonItem implements Item {
		private int myActionId;
		private String myIconName;
		private ZLResource myTooltip;
		private	ButtonGroup myButtonGroup;
		
		public ButtonItem(int actionId, String iconName, ZLResource tooltip) {
			myActionId = actionId;
			myIconName = iconName;
			myTooltip = tooltip;
		}

		public int getActionId() {
			return myActionId;
		}
		
		public String getIconName() {
			return myIconName;
		}
		
		public String getTooltip() {
			if (!myTooltip.hasValue()) {
				return "";
			}
			return myTooltip.value();
		}

		public ButtonGroup getButtonGroup() {
			return myButtonGroup;
		}
		
		public boolean isToggleButton() {
			return myButtonGroup != null;
		}
		
		public void press() {
			if (isToggleButton()) { 
				myButtonGroup.press(this);
			}
		}
		
		public boolean isPressed() {
			return isToggleButton() && (this == myButtonGroup.PressedItem);
		}

		public void setButtonGroup(ButtonGroup bg) {
			if (myButtonGroup != null) {
				myButtonGroup.Items.remove(this);
			}
			
			myButtonGroup = bg;
			
			if (myButtonGroup != null) {
				myButtonGroup.Items.add(this);
			}
		}	
	}
	
	public class SeparatorItem implements Item {
	}
	
	public class OptionEntryItem implements Item {
		//private ZLOptionEntry myOptionEntry;

		//public OptionEntryItem(ZLOptionEntry entry) {
			//myOptionEntry = entry;
		//}
			
		//public ZLOptionEntry entry() {
		//	return myOptionEntry;
		//}
	}

	public class ButtonGroup {
		public int UnselectAllButtonsActionId;
		public	Set<ButtonItem> Items = new HashSet<ButtonItem>();
		public	ButtonItem PressedItem;

		public ButtonGroup(int unselectAllButtonsActionId) {
			UnselectAllButtonsActionId = unselectAllButtonsActionId;
			PressedItem = null;
		}
		
		public	void press(ButtonItem item) {
			PressedItem = item;
		}
	}
}
