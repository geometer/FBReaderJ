package org.zlibrary.core.optionentries;

import org.zlibrary.core.dialogs.ZLBooleanOptionEntry;
import org.zlibrary.core.options.ZLBooleanOption;

public class ZLSimpleBooleanOptionEntry extends ZLBooleanOptionEntry {
	private ZLBooleanOption myOption;
	
	public ZLSimpleBooleanOptionEntry(ZLBooleanOption option) {
		myOption = option;
	}
	
	@Override
	public boolean initialState() {
		return myOption.getValue();
	}

	@Override
	public void onAccept(boolean state) {
		myOption.setValue(state);
	}
}
