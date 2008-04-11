package org.geometerplus.zlibrary.core.optionEntries;

import org.geometerplus.zlibrary.core.dialogs.ZLStringOptionEntry;

public class ZLStringInfoEntry extends ZLStringOptionEntry {
	private String myValue;
	
	public ZLStringInfoEntry(String value) {
		myValue = value;
		setActive(false);
	}
	
	public ZLStringInfoEntry(int value) {
		myValue = String.valueOf(value);
		setActive(false);
	}
	
	public String initialValue() {
		return myValue;
	}

	public void onAccept(String value) {}
}
