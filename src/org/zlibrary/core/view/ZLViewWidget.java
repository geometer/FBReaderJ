package org.zlibrary.core.view;

abstract public class ZLViewWidget {

	private ZLView myView;
	private Angle myRotation;

	public enum Angle {
		DEGREES0,
		DEGREES90,
		DEGREES180,
		DEGREES270
	};

	protected ZLViewWidget(Angle initialAngle) {
		myRotation = initialAngle;
	}

	public void setView(ZLView view) {
		myView = view;
	}

	public ZLView getView() {
		return myView;
	}

	abstract public void trackStylus(boolean track);

	public void rotate(Angle rotation) {
		myRotation = rotation;
	}

	public Angle getRotation() {
		return myRotation;
	}

	abstract protected void repaint();
}
