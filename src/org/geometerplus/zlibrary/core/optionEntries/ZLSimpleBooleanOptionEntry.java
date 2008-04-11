package org.geometerplus.zlibrary.core.optionEntries;

import org.geometerplus.zlibrary.core.dialogs.ZLBooleanOptionEntry;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;

public class ZLSimpleBooleanOptionEntry extends ZLBooleanOptionEntry {
	private ZLBooleanOption myOption;
	
	public ZLSimpleBooleanOptionEntry(ZLBooleanOption option) {
		myOption = option;
	}
	
	public boolean initialState() {
		return myOption.getValue();
	}

	public void onAccept(boolean state) {
		myOption.setValue(state);
	}
}
