package org.geometerplus.zlibrary.core.dialogs;

public abstract class ZLBoolean3OptionEntry extends ZLOptionEntry {
	protected ZLBoolean3OptionEntry() {
	}
	
	public int getKind() {
		return ZLOptionKind.BOOLEAN3;
	}

	public void onStateChanged(int state) {	
	}
	
	public abstract int initialState();
	
	public abstract void onAccept(int state);
}
