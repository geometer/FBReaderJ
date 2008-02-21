package org.zlibrary.core.optionentries;

import org.zlibrary.core.dialogs.ZLBoolean3OptionEntry;
import org.zlibrary.core.options.ZLBoolean3Option;
import org.zlibrary.core.options.ZLBooleanOption;
import org.zlibrary.core.util.ZLBoolean3;

public class ZLSimpleBoolean3OptionEntry extends ZLBoolean3OptionEntry {
	private ZLBoolean3Option myOption;
	
	public ZLSimpleBoolean3OptionEntry(ZLBoolean3Option option) {
		myOption = option;
	}
	
	public int initialState() {
		return myOption.getValue();
	}

	public void onAccept(int state) {
		myOption.setValue(state);
	}
}
