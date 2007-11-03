package org.zlibrary.core.application;

import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLViewWidget;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.core.library.ZLibrary;

public class ZLApplication {
	protected ZLApplication(String name) {
		myName = name;
		myContext = ZLibrary.getContext();
		// TODO: implement
	}

	protected void setView(ZLView view) {
		if (view == null) {
			return;
		}

		if (myViewWidget != null) {
			myViewWidget.setView(view);
			resetWindowCaption();
			refreshWindow();
		} else {
			myInitialView = view;
		}
	}

	protected ZLView getCurrentView() {
		return (myViewWidget != null) ? myViewWidget.getView() : null;
	}

	protected void quit() {
		// TODO: implement
	}

	void setWindow(ZLApplicationWindow window) {
		myWindow = window;
	}

	public void initWindow() {
		myViewWidget = myWindow.createViewWidget();
		myWindow.init();
		setView(myInitialView);
	}

	public ZLPaintContext getContext() {
		return myContext;
	}

	public void refreshWindow() {
		// TODO: implement
	}

	public void resetWindowCaption() {
		if (myWindow != null) {
			ZLView view = getCurrentView();
			if (view != null) {
				myWindow.setCaption(view.caption());
			}
		}
	}

	private String myName;
	private ZLViewWidget myViewWidget;
	private ZLApplicationWindow myWindow;
	private ZLPaintContext myContext;
	private ZLView myInitialView;
}
