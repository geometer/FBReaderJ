package org.zlibrary.core.dialogs;

public abstract class ZLOptionView {
	private boolean myInitialized;
	protected final String myName;
	protected final String myTooltip;
	protected final ZLOptionEntry myOption;
	
	public ZLOptionView(String name, String tooltip, ZLOptionEntry option) {
		myName = name;
		myOption = option;
		myTooltip = tooltip;
		myInitialized = false;
		myOption.setView(this);
	}

	public void reset() {}
	
	protected void _setActive(boolean active) {}	

	public final void setVisible(boolean visible) {
		if (visible) {
			if (!myInitialized) {
				createItem();
				myInitialized = true;
			}
			setActive(myOption.isActive());
			show();
		} else {
			if (myInitialized) {
				hide();
			}
		}
	}
	
	public final void setActive(boolean active) {
		if (myInitialized) {
			_setActive(active);
		}
	}
	
	public final void onAccept() {
		if (myInitialized) {
			_onAccept();
		}
	}

	protected abstract void createItem();
	
	protected abstract void hide();
	
	protected abstract void show();
		
	protected abstract void _onAccept();

	protected final String getName() {
		return myName;	
	}
	
	protected final String getTooltip() {
		return myTooltip;
	}
}
