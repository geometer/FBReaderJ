package org.zlibrary.core.application;

public class FullscreenAction extends ZLAction {
	private ZLApplication myApplication;
	private	boolean myIsToggle;

	public FullscreenAction(ZLApplication application, boolean toggle) {
		this.myApplication = application;
		this.myIsToggle = toggle;
	}
	
	public boolean isVisible() {
		return myIsToggle || !myApplication.isFullscreen();
	}
	
	public void run() {
		myApplication.setFullscreen(!myApplication.isFullscreen());
	}
}
