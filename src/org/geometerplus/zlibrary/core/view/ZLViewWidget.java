package org.geometerplus.zlibrary.core.view;

abstract public class ZLViewWidget {
	private ZLView myView;
	private int myRotation;

	public interface Angle {
		int DEGREES0 = 0;
		int DEGREES90 = 90;
		int DEGREES180 = 180;
		int DEGREES270 = 270;
	};

	protected ZLViewWidget(int initialAngle) {
		myRotation = initialAngle;
	}

	public final void setView(ZLView view) {
		myView = view;
	}

	public final ZLView getView() {
		return myView;
	}

	abstract public void trackStylus(boolean track);

	public final void rotate(int rotation) {
		myRotation = rotation;
	}

	public final int getRotation() {
		return myRotation;
	}

	// TODO: change to protected
	abstract public void repaint();
}
