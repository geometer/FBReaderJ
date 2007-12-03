package org.zlibrary.text.view.impl;

/*package*/ class ZLTextRectangularArea {
	public int XStart;	
	public int XEnd;	
	public int YStart;	
	public int YEnd;	
	
	public ZLTextRectangularArea(int xStart, int xEnd, int yStart, int yEnd) {
		XStart = xStart;
		XEnd = xEnd;
		YStart = yStart;
		YEnd = yEnd;
	}

	class RangeChecker {
		private int myX;
		private int myY;
		
		public RangeChecker(int x, int y) {
			myX = x;
			myY = y;
		}

		public boolean inside(ZLTextRectangularArea position) {
			return (position.XStart <= myX && myX <= position.XEnd &&
				position.YStart <= myY && myY <= position.YEnd);
		}
	}
}
