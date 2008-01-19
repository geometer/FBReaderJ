package org.zlibrary.ui.j2me.view;

import org.zlibrary.core.view.ZLViewWidget;

public class ZLJ2MEViewWidget extends ZLViewWidget {
	private final ZLCanvas myCanvas;

	public ZLJ2MEViewWidget(ZLCanvas canvas) {
		super(Angle.DEGREES0);
		myCanvas = canvas;
		canvas.setViewWidget(this);
	}

	public void trackStylus(boolean track) {
		// TODO: implement
	}

	public void repaint() {
		myCanvas.repaint();
	}
}
