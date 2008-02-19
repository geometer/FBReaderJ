package org.zlibrary.core.dialogs;

import org.zlibrary.core.util.ZLBoolean3;

public abstract class ZLBoolean3OptionEntry extends ZLOptionEntry {
	protected ZLBoolean3OptionEntry() {
	}
	
	public int getKind() {
		return ZLOptionKind.BOOLEAN3;
	}

	public void onStateChanged(ZLBoolean3 state) {	
	}
	
	public abstract int initialState();
	
	public abstract void onAccept(int state);
}
