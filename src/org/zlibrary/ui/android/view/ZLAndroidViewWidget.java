package org.zlibrary.ui.android.view;

import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLViewWidget;

public class ZLAndroidViewWidget extends ZLViewWidget {
	public static final ZLAndroidViewWidget Instance = new ZLAndroidViewWidget(Angle.DEGREES0);

	private ZLAndroidViewWidget(Angle initialAngle) {
		super(initialAngle);
		// TODO: implement
	}

	public void repaint() {
		// TODO: implement
	}

	public void setView(ZLView view) {
		// TODO: implement
		super.setView(view);
	}

	public void trackStylus(boolean track) {
		// TODO: implement
	}
}
