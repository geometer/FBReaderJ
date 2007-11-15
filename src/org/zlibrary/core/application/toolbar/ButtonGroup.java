package org.zlibrary.core.application.toolbar;

import java.util.HashSet;
import java.util.Set;

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
