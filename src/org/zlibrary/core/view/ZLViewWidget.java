package org.zlibrary.core.view;

abstract public class ZLViewWidget {
	private ZLView myView;
	private Angle myRotation;

	public enum Angle {
		DEGREES0(0),
		DEGREES90(90),
		DEGREES180(180),
		DEGREES270(270);
		
		private Angle(int degrees) {
			myDegrees = degrees;
		}
		
		public int getDegrees() {
			return myDegrees;
		}
		
		static public Angle getByDegrees(int degrees) {
			switch(degrees) {
				default:
				case 0: 
					return Angle.DEGREES0;
				case 90:
					return Angle.DEGREES90;
				case 180:
					return Angle.DEGREES180;
				case 270:
					return Angle.DEGREES270;
			}
		}

		private final int myDegrees;
	};

	protected ZLViewWidget(Angle initialAngle) {
		myRotation = initialAngle;
	}

	public final void setView(ZLView view) {
		myView = view;
	}

	public final ZLView getView() {
		return myView;
	}

	abstract public void trackStylus(boolean track);

	public final void rotate(Angle rotation) {
		myRotation = rotation;
	}

	public final Angle getRotation() {
		return myRotation;
	}

	// TODO: change to protected   
	abstract public void repaint();
}
