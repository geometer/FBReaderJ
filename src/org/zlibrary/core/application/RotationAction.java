package org.zlibrary.core.application;

import org.zlibrary.core.view.ZLViewWidget;

public class RotationAction extends ZLAction {
	private ZLApplication myApplication;

	public RotationAction(ZLApplication application) {
		myApplication = application;
	}
	
	public boolean isVisible() {
		return (myApplication.getMyViewWidget() != null) &&
		 ((myApplication.getRotationAngleOption().getValue() != ZLViewWidget.Angle.DEGREES0.getAngle()) ||
			(myApplication.getMyViewWidget().getRotation() != ZLViewWidget.Angle.DEGREES0));

	}
	
	public void run() {
		int optionValue = (int)myApplication.getRotationAngleOption().getValue();
		ZLViewWidget.Angle oldAngle = myApplication.getMyViewWidget().getRotation();
		ZLViewWidget.Angle newAngle = ZLViewWidget.Angle.DEGREES0;
		if (optionValue == -1) {
			switch (oldAngle) {
				case DEGREES0:
					newAngle = ZLViewWidget.Angle.DEGREES90;
					break;
				case DEGREES90:
					newAngle = ZLViewWidget.Angle.DEGREES180;
					break;
				case DEGREES180:
					newAngle = ZLViewWidget.Angle.DEGREES270;
					break;
				case DEGREES270:
					newAngle = ZLViewWidget.Angle.DEGREES0;
					break;
			}
		} else {
			//newAngle = (oldAngle == ZLViewWidget.Angle.DEGREES0) ?
			//(ZLViewWidget.Angle)optionValue : ZLViewWidget.Angle.DEGREES0;
		}
		myApplication.getMyViewWidget().rotate(newAngle);
		myApplication.getAngleStateOption().setValue(newAngle.getAngle());
		myApplication.refreshWindow();		
	}
}
