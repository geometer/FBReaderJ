/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.ui.j2me.view;

import javax.microedition.lcdui.*;

import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.ui.j2me.image.ZLJ2MEImageData;

import org.geometerplus.zlibrary.core.view.ZLPaintContext;

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
		if (myGraphics != null) {
			final int style = (bold ? Font.STYLE_BOLD : Font.STYLE_PLAIN) |
												(italic ? Font.STYLE_ITALIC : Font.STYLE_PLAIN);
			myGraphics.setFont(Font.getFont(Font.FACE_PROPORTIONAL, style, Font.SIZE_MEDIUM));
		}
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

	public int imageWidth(ZLImageData data) {
		return ((ZLJ2MEImageData)data).getImage().getWidth();
	}

	public int imageHeight(ZLImageData data) {
		return ((ZLJ2MEImageData)data).getImage().getHeight();
	}

	public void drawImage(int x, int y, ZLImageData data) {
		myGraphics.drawImage(((ZLJ2MEImageData)data).getImage(), x, y, Graphics.BOTTOM | Graphics.LEFT);
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
