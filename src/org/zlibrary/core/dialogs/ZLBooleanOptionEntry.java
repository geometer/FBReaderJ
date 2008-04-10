package org.zlibrary.core.dialogs;

public abstract class ZLBooleanOptionEntry extends ZLOptionEntry {
	protected ZLBooleanOptionEntry() {
		
	}
	
	public int getKind() {
		return ZLOptionKind.BOOLEAN;
	}

	public void onStateChanged(boolean state) {	
	}
	
	public abstract boolean initialState();
	
	public abstract void onAccept(boolean state);

	public void onReset() {
	}
}
