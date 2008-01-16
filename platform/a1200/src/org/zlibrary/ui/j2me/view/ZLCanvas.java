package org.zlibrary.ui.j2me.view;

import javax.microedition.lcdui.*;

import org.zlibrary.core.view.ZLPaintContext;

public class ZLCanvas extends Canvas {
	private final ZLJ2MEPaintContext myContext = new ZLJ2MEPaintContext(this);
	private ZLJ2MEViewWidget myWidget;

	void setViewWidget(ZLJ2MEViewWidget widget) {
		myWidget = widget;
	}

	public ZLPaintContext getContext() {
		return myContext;
	}

	public void paint(Graphics g) {
		myContext.begin(g);
		myWidget.getView().paint();
		myContext.end();
	}

	public void keyPressed(int keyCode) {
		//repaint();
	}
}
