package org.geometerplus.zlibrary.ui.android.dialogs;

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.dialogs.*;

class ZLAndroidOptionsTab extends ZLDialogContent {
	static class OptionData {
		final String Name;
		final ZLOptionEntry Entry;

		OptionData(String name, ZLOptionEntry entry) {
			Name = name;
			Entry = entry;
		}
	}
	final ArrayList<OptionData> myEntries = new ArrayList<OptionData>();

	ZLAndroidOptionsTab(ZLResource resource) {
		super(resource);
		// TODO: implement
	}

	public void addOptionByName(String name, ZLOptionEntry option) {
		if (option != null) {
			myEntries.add(new OptionData(name, option));
		}
	}

	public void addOptionsByNames(String name0, ZLOptionEntry option0, String name1, ZLOptionEntry option1) {
		addOptionByName(name0, option0);
		addOptionByName(name1, option1);
	}
}
