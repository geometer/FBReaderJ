package org.zlibrary.ui.android.dialogs;

import android.app.Activity;
import android.view.View;

import org.zlibrary.core.dialogs.*;
import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.runnable.ZLRunnable;
import org.zlibrary.ui.android.application.ZLAndroidApplicationWindow;

import org.zlibrary.ui.android.library.*;

public class ZLAndroidDialogManager extends ZLDialogManager {
	//private ZLAndroidApplicationWindow myApplicationWindow;
	private final Activity myActivity;
	
	public ZLAndroidDialogManager(Activity activity) {
		myActivity = activity;
	}
	
	public boolean runSelectionDialog(String key, ZLTreeHandler handler) {
		boolean code = new ZLAndroidSelectionDialog(myActivity, getDialogTitle(key), handler).run();
		View mainView = ((ZLAndroidLibrary)ZLAndroidLibrary.getInstance()).getWidget();
		myActivity.setContentView(mainView);
		mainView.requestFocus();
		return code;
	}

	public void showErrorBox(String key, String message) {
		// TODO: implement
		//JOptionPane.showMessageDialog(myApplicationWindow.getFrame(), message, getDialogTitle(key), 1, null);
	}

	public void showInformationBox(String key, String message) {
		// TODO: implement
		//JOptionPane.showMessageDialog(myApplicationWindow.getFrame(), message, getDialogTitle(key), 1, null);
	}

	public int showQuestionBox(String key, String message, String button0, String button1, String button2) {
		// TODO: implement
		return 0;
	}

	public ZLAndroidApplicationWindow createApplicationWindow(ZLApplication application) {
		// TODO: implement
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
		// TODO: implement
		//return new ZLAndroidOptionsDialog(myApplicationWindow.getFrame(), getResource().getResource(key), applyAction, showApplyButton);
		return null;
	}
}
