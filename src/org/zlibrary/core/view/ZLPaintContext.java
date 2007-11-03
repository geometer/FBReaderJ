package org.zlibrary.core.view;

import org.zlibrary.options.util.ZLColor;

abstract public class ZLPaintContext {
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

	//std::vector<std::string> &fontFamilies();
	//abstract public std::string realFontFamilyName(std::string &fontFamily);
	//abstract protected void fillFamiliesList(std::vector<std::string> &families);

	//mutable std::vector<std::string> myFamilies;

	private int myX;
	private int myY;
}
