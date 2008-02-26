package org.zlibrary.ui.android.dialogs;

import org.zlibrary.core.dialogs.*;
import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.runnable.ZLRunnable;
import org.zlibrary.ui.android.application.ZLAndroidApplicationWindow;

public class ZLAndroidDialogManager extends ZLDialogManager {
	//private ZLAndroidApplicationWindow myApplicationWindow;
	
	public ZLAndroidDialogManager() {
	}
	
	public boolean runSelectionDialog(String key, ZLTreeHandler handler) {
		return new ZLAndroidSelectionDialog(getDialogTitle(key), handler).run();
	}

	public void showErrorBox(String key, String message) {
		//JOptionPane.showMessageDialog(myApplicationWindow.getFrame(), message, getDialogTitle(key), 1, null);
	}

	public void showInformationBox(String key, String message) {
		//JOptionPane.showMessageDialog(myApplicationWindow.getFrame(), message, getDialogTitle(key), 1, null);
	}

	public ZLAndroidApplicationWindow createApplicationWindow(ZLApplication application) {
		//myApplicationWindow = new ZLAndroidApplicationWindow(application);
		//return myApplicationWindow;
		return null;
	}

	/*
	static JButton createButton(String key) {
		String text = getButtonText(key).replace("&", "");
		return new JButton(text);
	}
	*/

	public ZLOptionsDialog createOptionsDialog(String key, ZLRunnable applyAction, boolean showApplyButton) {
		//return new ZLAndroidOptionsDialog(myApplicationWindow.getFrame(), getResource().getResource(key), applyAction, showApplyButton);
		return null;
	}
}
