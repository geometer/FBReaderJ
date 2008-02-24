package org.zlibrary.core.optionEntries;

import org.zlibrary.core.dialogs.ZLStringOptionEntry;
import org.zlibrary.core.options.ZLStringOption;

public class ZLSimpleStringOptionEntry extends ZLStringOptionEntry {
	private ZLStringOption myOption;
	
	public ZLSimpleStringOptionEntry(ZLStringOption option) {
		myOption = option;
	}
	
	public String initialValue() {
		return myOption.getValue();
	}

	public void onAccept(String value) {
		myOption.setValue(value);
	}
}
