package org.zlibrary.core.application;

public class FullscreenAction extends Action {
	private ZLApplication myApplication;
	private	boolean myIsToggle;

	public FullscreenAction(ZLApplication application, boolean toggle) {
		this.myApplication = application;
		this.myIsToggle = toggle;
	}
	
	public boolean isVisible() {
		return true;//myIsToggle || !myApplication.isFullscreen();
	}
	
	public void run() {
		//myApplication.setFullscreen(!myApplication.isFullscreen());
	}
}
