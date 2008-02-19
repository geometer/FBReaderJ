package org.zlibrary.core.dialogs;

public abstract class ZLOptionView { // ? public
	private boolean myInitialized;
	protected final String myName;
	protected final String myTooltip;
	protected final ZLOptionEntry myOption;
	
	public ZLOptionView(String name, String tooltip, ZLOptionEntry option) {
		myName = name;
		myOption = option;
		myTooltip = tooltip;
		myInitialized = false;
//		myOption.SetView(this);
	}

	// TODO: change to pure virtual
	public void reset() {
		
	}

	public void setVisible(boolean visible) {
		if (visible) {
			if (!myInitialized) {
				createItem();
				myInitialized = true;
			}
		//	setActive(myOption.isActive());
			show();
		} else {
			if (myInitialized) {
				hide();
			}
		}
	}
	
	public void setActive(boolean active) {
		if (myInitialized) {
			_setActive(active);
		}
	}
	
	public void onAccept() {
		if (myInitialized) {
			_onAccept();
		}
	}

	protected abstract void createItem();
	
	protected abstract void hide();
	
	protected abstract void show();
	
	// TODO: replace by pure virtual method
	protected void _setActive(boolean active) {
		
	}
		
	protected abstract void _onAccept();

	protected String getName() {
		return myName;	
	}
	
	protected String getTooltip() {
		return myTooltip;
	}
}
