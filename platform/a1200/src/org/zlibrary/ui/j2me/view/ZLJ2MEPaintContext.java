package org.zlibrary.ui.j2me.view;

import org.zlibrary.core.util.ZLColor;
import org.zlibrary.core.image.ZLImageData;

import org.zlibrary.core.view.ZLPaintContext;

public class ZLJ2MEPaintContext extends ZLPaintContext {
	public void clear(ZLColor color) {
		// TODO: implement
	}

	protected void setFontInternal(String family, int size, boolean bold, boolean italic) {
		// TODO: implement
	}

	public void setColor(ZLColor color, int style) {
		// TODO: implement
	}

	public void setFillColor(ZLColor color, int style) {
		// TODO: implement
	}

	public int getWidth() {
		// TODO: implement
		return 100;
	}

	public int getHeight() {
		// TODO: implement
		return 100;
	}
	
	public int getStringWidth(char[] string, int offset, int length) {
		// TODO: implement
		return 10 * length;
	}

	protected int getSpaceWidthInternal() {
		// TODO: implement
		return 10;
	}

	protected int getStringHeightInternal() {
		// TODO: implement
		return 10;
	}

	protected int getDescentInternal() {
		// TODO: implement
		return 0;
	}

	public void drawString(int x, int y, char[] string, int offset, int length) {
		// TODO: implement
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
		// TODO: implement
	}

	public void fillRectangle(int x0, int y0, int x1, int y1) {
		// TODO: implement
	}

	public void drawFilledCircle(int x, int y, int r) {
		// TODO: implement
	}
}
