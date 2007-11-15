package org.zlibrary.core.view;

abstract public class ZLViewWidget {

	private ZLView myView;
	private Angle myRotation;

	public enum Angle {
		DEGREES0(0),
		DEGREES90(90),
		DEGREES180(180),
		DEGREES270(270);
		
		private Angle(int angle) {
			this.angle = angle;
		}
		
		private int angle;
		
		public int getAngle() {
			return angle;
		}
		
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
    //chabge to protected   
	abstract public void repaint();
}
