package org.zlibrary.core.optionentries;

import org.zlibrary.core.dialogs.ZLStringOptionEntry;
import org.zlibrary.core.options.ZLStringOption;

public class ZLSimpleStringOptionEntry extends ZLStringOptionEntry {
	private ZLStringOption myOption;
	
	public ZLSimpleStringOptionEntry(ZLStringOption option) {
		myOption = option;
	}
	
	@Override
	public String initialValue() {
		return myOption.getValue();
	}

	@Override
	public void onAccept(String value) {
		myOption.setValue(value);
	}
}
