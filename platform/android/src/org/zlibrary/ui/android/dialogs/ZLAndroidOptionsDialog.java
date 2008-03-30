package org.zlibrary.ui.android.dialogs;

import android.content.Context;
/*
import android.app.Dialog;
import android.os.*;
import android.view.*;
import android.widget.*;
*/

import org.zlibrary.core.dialogs.ZLOptionsDialog;
import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.resources.ZLResource;

class ZLAndroidOptionsDialog extends ZLOptionsDialog {
	// TODO: remove
	private final Context myContext;

	ZLAndroidOptionsDialog(Context context, ZLResource resource, Runnable applyAction) {
		super(resource, applyAction);
		myContext = context;
		// TODO: implement
	}

	protected String getSelectedTabKey() {
		// TODO: implement
		return "";
	}
	
	protected void selectTab(String key) {
		// TODO: implement
	}
	
	protected void runInternal() {
		// TODO: implement
		accept();
	}
	
	public ZLDialogContent createTab(String key) {
		return new ZLAndroidDialogContent(myContext, getTabResource(key), null);
	}
}
