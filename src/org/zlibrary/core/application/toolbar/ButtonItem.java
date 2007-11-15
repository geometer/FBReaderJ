package org.zlibrary.core.application.toolbar;

import org.zlibrary.core.resources.ZLResource;

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

	public Type getType() {
		return Type.BUTTON;
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
