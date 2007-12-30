package org.zlibrary.ui.android.view;

import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLViewWidget;

import org.zlibrary.ui.android.library.ZLAndroidLibrary;

public class ZLAndroidViewWidget extends ZLViewWidget {
	private final ZLAndroidWidget myWidget = 
		((ZLAndroidLibrary)ZLAndroidLibrary.getInstance()).getWidget();

	public ZLAndroidViewWidget(int initialAngle) {
		super(initialAngle);
		myWidget.setViewWidget(this);
	}

	public void repaint() {
		// I'm not sure about threads, so postInvalidate() is used instead of invalidate()
		myWidget.postInvalidate();
	}

	public void trackStylus(boolean track) {
		// TODO: implement
	}
}
