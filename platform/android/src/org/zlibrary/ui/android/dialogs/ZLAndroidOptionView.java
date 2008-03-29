package org.zlibrary.ui.android.dialogs;

import android.view.View;

import org.zlibrary.core.dialogs.ZLOptionView;
import org.zlibrary.core.dialogs.ZLOptionEntry;

abstract class ZLAndroidOptionView extends ZLOptionView {
	View myView;
	ZLAndroidDialogContent myTab;
	private boolean myIsVisible;

	protected ZLAndroidOptionView(ZLAndroidDialogContent tab, String name, ZLOptionEntry option) {
		super(name, option);
		//setVisible(option.isVisible());
		myTab = tab;
	}

	View getAndroidView() {
		setVisible(true);
		return myView;
	}

	boolean isVisible() {
		return myIsVisible;
	}

	protected void show() {
		myIsVisible = true;
	}

	protected void hide() {
		myIsVisible = false;
	}

	protected void _setActive(boolean active) {
		// TODO: implement
	}
}
