package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import android.graphics.Point;

public class TapZoneAction extends FBAction {
	private final String myAction;
	TapZoneAction(FBReader fbreader, String action) {
		super(fbreader);
		myAction = action;
	}

	public boolean isEnabled() {
		ZLAndroidWidget widget = ((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getWidget();

		return true;
	}

	public void run() {
		ZLAndroidWidget widget = ((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getWidget();
		if (myAction.equals(ActionCode.TAP_ZONES)){
			ZLApplication.Instance().repaintView();
		}

		if (myAction.equals(ActionCode.TAP_ZONE_ADD)){
			ZLApplication.Instance().repaintView();
		}

		if (myAction.equals(ActionCode.TAP_ZONE_DELETE)){

		}

		if (myAction.equals(ActionCode.TAP_ZONES_SAVE)){
			ZLApplication.Instance().repaintView();
		}

		if (myAction.equals(ActionCode.TAP_ZONES_CANCEL)){
			ZLApplication.Instance().repaintView();
		}

		if (myAction.equals(ActionCode.TAP_ZONE_SELECT_ACTION)){
		}
	}
}
