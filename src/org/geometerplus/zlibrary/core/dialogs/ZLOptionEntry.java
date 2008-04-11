package org.geometerplus.zlibrary.core.dialogs;

public abstract class ZLOptionEntry {
	private ZLOptionView myView;
	private	boolean myIsVisible;
	private	boolean myIsActive;
		
	public ZLOptionEntry() {
		myIsVisible = true;
		myIsActive = true;
	}
	
	public abstract int getKind();

	public final void setView(ZLOptionView view) {
		myView = view;
	}
	
	public final void resetView() {
		if (myView != null) {
			myView.reset();
		}
	}

	public final boolean isVisible() {
		return myIsVisible;
	}
	
	public final boolean isActive() {
		return myIsActive;
	}
	
	public void setVisible(boolean visible) {
		myIsVisible = visible;
		if (myView != null) {
			myView.setVisible(visible);
		}
	}
	
	public void setActive(boolean active) {
		myIsActive = active;
		if (myView != null) {
			myView.setActive(active);
		}
	}
}
