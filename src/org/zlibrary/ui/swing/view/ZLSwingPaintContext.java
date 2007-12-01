package org.zlibrary.ui.swing.view;

import java.awt.*;


import org.zlibrary.core.options.util.ZLColor;
import org.zlibrary.core.view.ZLPaintContext;

public final class ZLSwingPaintContext extends ZLPaintContext {
	public void clear(ZLColor color) {
		// TODO: implement
	}

	public void setFont(String family, int size, boolean bold, boolean italic) {
		// TODO: implement
		if (myGraphics != null) {
			final int style = (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0);
			myGraphics.setFont(new Font("Default", style, size));
		}
	}

	public void setColor(ZLColor color, LineStyle style) {
		// TODO: implement
	}

	public void setFillColor(ZLColor color, FillStyle style) {
		// TODO: implement
	}

	void setSize(int w, int h) {
		myWidth = w;
		myHeight = h;
	}

	public int getWidth() {
		return myWidth;
	}

	public int getHeight() {
		return myHeight;
	}
	
	public int getStringWidth(String string) {
		return myGraphics.getFontMetrics().stringWidth(string);
	}

	public int getSpaceWidth() {
		// TODO: optimize (?)
		return myGraphics.getFontMetrics().charWidth(' ');
	}
	public int getStringHeight() {
		return myGraphics.getFontMetrics().getHeight();
	}
	public int getDescent() {
		return myGraphics.getFontMetrics().getDescent();
	}
	public void drawString(int x, int y, String string) {
		myGraphics.drawString(string, x, y);
	}

	public void drawLine(int x0, int y0, int x1, int y1) {
		myGraphics.drawLine(x0, y0, x1, y1);
	}

	public void fillRectangle(int x0, int y0, int x1, int y1) {
		if (x0 > x1) {
			int swap = x0;
			x0 = x1;
			x1 = swap;
		}
		if (y0 > y1) {
			int swap = y0;
			y0 = y1;
			y1 = swap;
		}
		myGraphics.fillRect(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
	}

	public void drawFilledCircle(int x, int y, int r) {
		// TODO: implement
	}

	void setGraphics(Graphics2D g) {
		myGraphics = g;
	}

	private Graphics2D myGraphics;
	private int myWidth;
	private int myHeight;
}
