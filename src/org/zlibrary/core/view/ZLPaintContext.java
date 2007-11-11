package org.zlibrary.core.view;

import java.util.LinkedList;
import java.util.List;

import org.zlibrary.options.util.ZLColor;

abstract public class ZLPaintContext {

	private int myX;
	private int myY;
	
	private int myLeftMargin;
	private int myRightMargin;
	private int myTopMargin;
	private int myBottomMargin;
	
	private List<String> myFamilies = new LinkedList<String>();

	public enum LineStyle {
		SOLID_LINE,
		DASH_LINE,
	};

	public enum FillStyle {
		SOLID_FILL,
		HALF_FILL,
	};
	
	protected ZLPaintContext() {
	}

	public int getX() {
		return myX;
	}

	public int getY() {
		return myY;
	}

	public void moveXTo(int x) {
		myX = x;
	}

	public void moveX(int deltaX) {
		myX += deltaX;
	}

	public void moveYTo(int y) {
		myY = y;
	}

	public void moveY(int deltaY) {
		myY += deltaY;
	}

	abstract public void clear(ZLColor color);

	abstract public void setFont(String family, int size, boolean bold, boolean italic);

	final public void setColor(ZLColor color) {
		setColor(color, LineStyle.SOLID_LINE);
	}
	abstract public void setColor(ZLColor color, LineStyle style);

	final public void setFillColor(ZLColor color) {
		setFillColor(color, FillStyle.SOLID_FILL);
	}
	abstract public void setFillColor(ZLColor color, FillStyle style);

	abstract public int getWidth();
	abstract public int getHeight();
	
	abstract public int stringWidth(String string/*, int len*/);
	abstract public int spaceWidth();
	abstract public int stringHeight();
	abstract public int descent();
	abstract public void drawString(int x, int y, String string/*, int len*/);

	//int imageWidth(ZLImageData &image);
	//int imageHeight(ZLImageData &image);
	//abstract public void drawImage(int x, int y, ZLImageData &image);

	abstract public void drawLine(int x0, int y0, int x1, int y1);
	abstract public void fillRectangle(int x0, int y0, int x1, int y1);
	abstract public void drawFilledCircle(int x, int y, int r);

	public void setMyLeftMargin(int margin) {
		if (getWidth() + myLeftMargin - margin > 0) {
			myLeftMargin = margin;
		}
	}

	public int getMyLeftMargin() {
		return myLeftMargin;
	}

	public void setMyRightMargin(int margin) {
		if (getWidth() + myRightMargin - margin > 0) {
			myRightMargin = margin;
		}
	}

	public int getMyRightMargin() {
		return myRightMargin;
	}

	public void setMyTopMargin(int margin) {
		if (getHeight() + myTopMargin - margin > 0) {
			myTopMargin = margin;
		}
	}

	public int getMyTopMargin() {
		return myTopMargin;
	}

	public void setMyBottomMargin(int margin) {
		if (getHeight() + myBottomMargin - margin > 0) {
			myBottomMargin = margin;
		}
	}

	public int getMyBottomMargin() {
		return myBottomMargin;
	}

	public List<String> fontFamilies() {
		if (myFamilies.isEmpty()) {
			fillFamiliesList(myFamilies);
		}
		return myFamilies;
	}
	
	abstract public String realFontFamilyName(String fontFamily);
	abstract protected void fillFamiliesList(List<String> families);
}


