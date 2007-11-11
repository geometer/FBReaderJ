package org.zlibrary.core.application.toolbar;

import java.util.Set;

public class ButtonGroup {
	public int UnselectAllButtonsActionId;
	public	Set<ButtonItem> Items;
	public	ButtonItem PressedItem;

	public ButtonGroup(int unselectAllButtonsActionId) {
		UnselectAllButtonsActionId = unselectAllButtonsActionId;
		PressedItem = null;
	}
	
	public	void press(ButtonItem item) {
		PressedItem = item;
	}
}
