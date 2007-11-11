package org.zlibrary.core.application;

public class RotationAction extends Action {
	private ZLApplication myApplication;

	public RotationAction(ZLApplication application) {
		myApplication = application;
	}
	
	public boolean isVisible() {
		return true;
		//return (myApplication.myViewWidget != 0) &&
		 //((myApplication.RotationAngleOption.value() != ZLViewWidget.DEGREES0) ||
			//(myApplication.myViewWidget->rotation() != ZLViewWidget.DEGREES0));

	}
	
	public void run() {
		/*int optionValue = myApplication.RotationAngleOption.value();
		ZLViewWidget.Angle oldAngle = myApplication.myViewWidget->rotation();
		ZLViewWidget.Angle newAngle = ZLViewWidget::DEGREES0;
		if (optionValue == -1) {
			switch (oldAngle) {
				case ZLViewWidget::DEGREES0:
					newAngle = ZLViewWidget::DEGREES90;
					break;
				case ZLViewWidget::DEGREES90:
					newAngle = ZLViewWidget::DEGREES180;
					break;
				case ZLViewWidget::DEGREES180:
					newAngle = ZLViewWidget::DEGREES270;
					break;
				case ZLViewWidget::DEGREES270:
					newAngle = ZLViewWidget::DEGREES0;
					break;
			}
		} else {
			newAngle = (oldAngle == ZLViewWidget::DEGREES0) ?
				(ZLViewWidget::Angle)optionValue : ZLViewWidget::DEGREES0;
		}
		myApplication.myViewWidget->rotate(newAngle);
		myApplication.AngleStateOption.setValue(newAngle);
		myApplication.refreshWindow();*/
		
	}
}
