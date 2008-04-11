package org.geometerplus.zlibrary.ui.android.dialogs;

import android.view.View;

import org.geometerplus.zlibrary.core.dialogs.ZLOptionView;
import org.geometerplus.zlibrary.core.dialogs.ZLOptionEntry;

abstract class ZLAndroidOptionView extends ZLOptionView {
	ZLAndroidDialogContent myTab;
	private boolean myIsVisible;

	protected ZLAndroidOptionView(ZLAndroidDialogContent tab, String name, ZLOptionEntry option) {
		super(name, option);
		myTab = tab;
	}

	boolean isVisible() {
		return myIsVisible;
	}

	protected void show() {
		myIsVisible = true;
		myTab.invalidateView();
	}

	protected void hide() {
		myIsVisible = false;
		myTab.invalidateView();
	}

	protected void _setActive(boolean active) {
		// TODO: implement
	}

	abstract void addAndroidViews();
}
