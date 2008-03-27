package org.zlibrary.ui.android.dialogs;

/*
import android.content.Context;
import android.app.Dialog;
import android.os.*;
import android.view.*;
import android.widget.*;
*/

import org.zlibrary.core.dialogs.ZLOptionsDialog;
import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.resources.ZLResource;

class ZLAndroidOptionsDialog extends ZLOptionsDialog {
	ZLAndroidOptionsDialog(ZLResource resource, Runnable applyAction) {
		super(resource, applyAction);
		// TODO: implement
	}

	protected String getSelectedTabKey() {
		// TODO: implement
		return "";
	}
	
	protected void selectTab(String key) {
		// TODO: implement
	}
	
	protected boolean runInternal() {
		// TODO: implement
		return true;
	}
	
	public ZLDialogContent createTab(String key) {
		return new ZLAndroidDialogContent(getTabResource(key));
	}
}
