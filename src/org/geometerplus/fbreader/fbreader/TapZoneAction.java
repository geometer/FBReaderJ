package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import android.graphics.Point;

public class TapZoneAction extends FBAction {
	private final String myAction;
	TapZoneAction(FBReaderApp fbreader, String action) {
		super(fbreader);
		myAction = action;
	}

	public boolean isEnabled() {
		ZLAndroidWidget widget = ((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getWidget();
		Point tapPoint = widget.getTapPoint();
		if (myAction.equals(ActionCode.TAP_ZONE_SELECT_ACTION) ||
			myAction.equals(ActionCode.TAP_ZONE_DELETE)) {
			return widget.myTapZones.selectZone(tapPoint.x, tapPoint.y);
		}

		return true;
	}

	public void run() {
		ZLAndroidWidget widget = ((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getWidget();
		if (myAction.equals(ActionCode.TAP_ZONES)){
			if (widget.myMode == ZLAndroidWidget.MODE_TAP_ZONES_EDIT) {
				widget.myTapZones.saveChanges();
				widget.myMode = ZLAndroidWidget.MODE_READ;
			}
			else if (widget.myMode == ZLAndroidWidget.MODE_READ) {
				widget.myTapZones.startEdit();
				widget.myMode = ZLAndroidWidget.MODE_TAP_ZONES_EDIT;
			}
			ZLApplication.Instance().repaintView();
		}

		if (myAction.equals(ActionCode.TAP_ZONE_ADD)){
			Point tapPoint = widget.getTapPoint();
			widget.myTapZones.addZoneByTap(tapPoint.x, tapPoint.y);
			ZLApplication.Instance().repaintView();
		}

		if (myAction.equals(ActionCode.TAP_ZONE_DELETE)){
			Point tapPoint = widget.getTapPoint();
			if (widget.myTapZones.selectZone(tapPoint.x, tapPoint.y)) {
				widget.myTapZones.deleteSelectedZone();
			}
		}

		if (myAction.equals(ActionCode.TAP_ZONES_SAVE)){
			widget.myTapZones.saveChanges();
			widget.myMode = ZLAndroidWidget.MODE_READ;
			ZLApplication.Instance().repaintView();
		}

		if (myAction.equals(ActionCode.TAP_ZONES_CANCEL)){
			widget.myTapZones.cancelChanges();
			widget.myMode = ZLAndroidWidget.MODE_READ;
			ZLApplication.Instance().repaintView();
		}

		if (myAction.equals(ActionCode.TAP_ZONE_SELECT_ACTION)){
			if (widget.myTapZones.mySelectedZone != null) {
				widget.myTapZones.mySelectedZone.selectAction();
			}
		}
	}
}
