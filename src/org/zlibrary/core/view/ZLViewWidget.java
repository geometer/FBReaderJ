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
			myAngle = angle;
		}
		
		public int getAngle() {
			return myAngle;
		}

		private int myAngle;
	};
	
	public Angle helperFunctionGetAngle(int myAngle) {
		switch(myAngle) {
		case 0: 
			return Angle.DEGREES0;
		case 90:
			return Angle.DEGREES90;
		case 180:
			return Angle.DEGREES180;
		case 270:
			return Angle.DEGREES270;
		default: 
			return null;
		}
	}

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
