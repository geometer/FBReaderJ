package org.geometerplus.zlibrary.core.optionEntries;

import org.geometerplus.zlibrary.core.dialogs.ZLBoolean3OptionEntry;
import org.geometerplus.zlibrary.core.options.ZLBoolean3Option;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;

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
