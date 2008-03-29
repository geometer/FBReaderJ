package org.zlibrary.core.dialogs;

import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.options.ZLSimpleOption;

public abstract class ZLDialog {
	protected ZLDialogContent myTab;	

	public ZLResource resource(final String key) {
		return myTab.getResource(key);
	}

	public abstract void addButton(final String key, Runnable action);

	public void addOptionByName(final String name, ZLOptionEntry entry) {
		myTab.addOptionByName(name, entry);
	}
	
	public void addOption(final String key, ZLOptionEntry entry) {
		myTab.addOption(key, entry);
	}
	
	public void addOption(final String key, ZLSimpleOption option) {
		myTab.addOption(key, option);
	}

	public abstract void run();

	public void acceptValues() {
		myTab.accept();
	}
}
