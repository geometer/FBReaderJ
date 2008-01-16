package org.zlibrary.ui.j2me.view;

import javax.microedition.lcdui.*;

import org.zlibrary.core.util.ZLColor;
import org.zlibrary.core.image.ZLImageData;

import org.zlibrary.core.view.ZLPaintContext;

class ZLJ2MEPaintContext extends ZLPaintContext {
	private final Canvas myCanvas;
	private Graphics myGraphics;

	private ZLColor myTextColor = new ZLColor(0, 0, 0);
	private ZLColor myFillColor = new ZLColor(0, 0, 0);
	private boolean myTextNotFillMode;

	ZLJ2MEPaintContext(Canvas canvas) {
		myCanvas = canvas;
	}

	void begin(Graphics g) {
		myGraphics = g;
	}

	void end() {
		myGraphics = null;
	}

	public void clear(ZLColor color) {
		myGraphics.setColor(color.Red, color.Green, color.Blue);
		myGraphics.fillRect(0, 0, myCanvas.getWidth(), myCanvas.getHeight());
		color = myTextNotFillMode ? myTextColor : myFillColor;
		myGraphics.setColor(color.Red, color.Green, color.Blue);
	}

	protected void setFontInternal(String family, int size, boolean bold, boolean italic) {
		// TODO: implement
	}

	public void setColor(ZLColor color, int style) {
		// TODO: use style
		if (color != myTextColor) {
			myTextColor = color;
			myGraphics.setColor(color.Red, color.Green, color.Blue);
			myTextNotFillMode = true;
		} else if (!myTextNotFillMode) {
			myGraphics.setColor(color.Red, color.Green, color.Blue);
			myTextNotFillMode = true;
		}
	}

	public void setFillColor(ZLColor color, int style) {
		// TODO: use style
		if (color != myFillColor) {
			myFillColor = color;
			myGraphics.setColor(color.Red, color.Green, color.Blue);
			myTextNotFillMode = false;
		} else if (!myTextNotFillMode) {
			myGraphics.setColor(color.Red, color.Green, color.Blue);
			myTextNotFillMode = false;
		}
	}

	public int getWidth() {
		return myCanvas.getWidth();
	}

	public int getHeight() {
		return myCanvas.getHeight();
	}
	
	public int getStringWidth(char[] string, int offset, int length) {
		// TODO: optimize
		return myGraphics.getFont().charsWidth(string, offset, length);
	}

	protected int getSpaceWidthInternal() {
		return myGraphics.getFont().charWidth(' ');
	}

	protected int getStringHeightInternal() {
		return myGraphics.getFont().getHeight();
	}

	protected int getDescentInternal() {
		// TODO: implement
		return 0;
	}

	public void drawString(int x, int y, char[] string, int offset, int length) {
		myGraphics.drawChars(string, offset, length, x, y, Graphics.BOTTOM | Graphics.LEFT);
	}

	public int imageWidth(ZLImageData image) {
		// TODO: implement
		return 10;
	}

	public int imageHeight(ZLImageData image) {
		// TODO: implement
		return 10;
	}

	public void drawImage(int x, int y, ZLImageData image) {
		// TODO: implement
	}

	public void drawLine(int x0, int y0, int x1, int y1) {
		myGraphics.drawLine(x0, y0, x1, y1);
	}

	public void fillRectangle(int x0, int y0, int x1, int y1) {
		// TODO: implement
	}

	public void drawFilledCircle(int x, int y, int r) {
		// TODO: implement
	}
}
