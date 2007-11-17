package org.zlibrary.ui.swing.view;

import java.awt.Component;
import java.awt.Graphics;

import org.zlibrary.options.util.ZLColor;

import org.zlibrary.core.view.ZLPaintContext;

public class ZLSwingPaintContext extends ZLPaintContext {
	public void clear(ZLColor color) {
		// TODO: implement
	}

	public void setFont(String family, int size, boolean bold, boolean italic) {
		// TODO: implement
	}

	public void setColor(ZLColor color, LineStyle style) {
		// TODO: implement
	}

	public void setFillColor(ZLColor color, FillStyle style) {
		// TODO: implement
	}

	public int getWidth() {
		return myComponent.getSize().width;
	}

	public int getHeight() {
		return myComponent.getSize().height;
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
		// TODO: implement
		return 0;
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

	void setComponent(Component component) {
		myComponent = component;
	}

	void setGraphics(Graphics g) {
		myGraphics = g;
	}

	private Component myComponent;
	private Graphics myGraphics;
}
