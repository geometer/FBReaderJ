package org.zlibrary.core.dialogs;

public abstract class ZLOptionView {
	protected final String myName;
	protected final ZLOptionEntry myOption;
	private boolean myInitialized;
	
	protected ZLOptionView(String name, ZLOptionEntry option) {
		myName = name;
		myOption = option;
		myInitialized = false;
		myOption.setView(this);
	}

	protected abstract void reset();

	public final void setActive(boolean active) {
		if (myInitialized) {
			_setActive(active);
		}
	}

	protected abstract void _setActive(boolean active);

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
	
	protected abstract void hide();
	
	protected abstract void show();
		
	public final void onAccept() {
		if (myInitialized) {
			_onAccept();
		}
	}

	protected abstract void _onAccept();

	protected abstract void createItem();
}
