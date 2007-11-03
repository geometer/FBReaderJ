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
	
	public int stringWidth(String string, int len) {
		// TODO: implement
		return 0;
	}
	public int spaceWidth() {
		// TODO: implement
		return 0;
	}
	public int stringHeight() {
		// TODO: implement
		return 0;
	}
	public int descent() {
		// TODO: implement
		return 0;
	}
	public void drawString(int x, int y, String string, int len) {
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

	void setComponent(Component component) {
		myComponent = component;
	}

	void setGraphics(Graphics g) {
		myGraphics = g;
	}

	private Component myComponent;
	private Graphics myGraphics;
}
