package org.geometerplus.zlibrary.core.optionEntries;

import org.geometerplus.zlibrary.core.dialogs.ZLStringOptionEntry;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

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
