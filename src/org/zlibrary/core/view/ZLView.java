package org.zlibrary.core.view;

import org.zlibrary.core.application.ZLApplication;

abstract public class ZLView {
	private ZLApplication myApplication;
	private ZLPaintContext myContext;

	public ZLView(ZLApplication application, ZLPaintContext context) {
		myApplication = application;
		myContext = context;
	}

	void setPaintContext(ZLPaintContext context) {
		myContext = context;
	}

	abstract public String caption();

	abstract public void paint();

	public ZLPaintContext getContext() {
		return myContext;
	}

	protected boolean onStylusPress(int x, int y) {
		return false;
	}

	protected boolean onStylusRelease(int x, int y) {
		return false;
	}

	protected boolean onStylusMove(int x, int y) {
		return false;
	}

	protected boolean onStylusMovePressed(int x, int y) {
		return false;
	}

	protected boolean onFingerTap(int x, int y) {
		return false;
	}

	protected ZLApplication application() {
		return myApplication;
	}
	
	public void repaintView() {
		myApplication.refreshWindow();
	}
}
