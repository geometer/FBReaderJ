package org.zlibrary.core.dialogs;

import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.options.ZLSimpleOption;

public abstract class ZLDialog {
	protected ZLDialogContent myTab;	

	public ZLResource resource(final String key) {
		return myTab.getResource(key);
	}

	public abstract void addButton(final String key, Runnable action);

	public void addOption(final String name, final String tooltip, ZLOptionEntry entry) {
		myTab.addOption(name, tooltip, entry);
	}
	
	public void addOption(final String name, ZLOptionEntry entry) {
		myTab.addOption(name, entry);
	}
	
	public void addOption(final String name, ZLSimpleOption option) {
		myTab.addOption(name, option);
	}

	public abstract void run();

	public void acceptValues() {
		myTab.accept();
	}
}
