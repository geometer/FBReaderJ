package org.fbreader.optionsDialog;

import java.util.HashMap;

import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLOptionEntry;

public class OptionsPage {
	private final HashMap /*<ZLOptionEntry, String>*/ myEntries = new HashMap();
	protected ComboOptionEntry myComboEntry;
	
	protected OptionsPage() {}

	protected void registerEntry(ZLDialogContent tab, final String entryKey, ZLOptionEntry entry, final String name) {
		if (entry != null) {
			entry.setVisible(false);
			myEntries.put(entry, name);
		}
		tab.addOption(entryKey, entry);
	}
	
	protected void registerEntries(ZLDialogContent tab, final String entry0Key,
		ZLOptionEntry entry0, final String entry1Key, ZLOptionEntry entry1, final String name) {
		if (entry0 != null) {
			entry0.setVisible(false);
			myEntries.put(entry0, name);
		}
		if (entry1 != null) {
			entry1.setVisible(false);
			myEntries.put(entry1, name);
		}
		tab.addOptions(entry0Key, entry0, entry1Key, entry1);
	}

	HashMap getEntries() {
		return myEntries;
	}
			
}
