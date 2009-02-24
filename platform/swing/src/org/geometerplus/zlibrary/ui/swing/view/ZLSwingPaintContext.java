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

package org.geometerplus.zlibrary.ui.swing.view;

import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.ui.swing.image.ZLSwingImageData;

public final class ZLSwingPaintContext extends ZLPaintContext {
	private Graphics2D myGraphics;
	private int myWidth;
	private int myHeight;
	private boolean myReversedX;
	private boolean myReversedY;
	private Color myBackgroundColor = new Color(255, 255, 255);
	private Color myColor = new Color(0, 0, 0);
	private Color myFillColor = new Color(255, 255, 255);

	private static boolean areSameColors(Color awtColor, ZLColor zlColor) {
		return
			(awtColor.getRed() == zlColor.Red) &&
			(awtColor.getGreen() == zlColor.Green) &&
			(awtColor.getBlue() == zlColor.Blue);
	}

	void setRotation(int rotation) {
		switch (rotation) {
			case ZLViewWidget.Angle.DEGREES0:
				myReversedX = false;
				myReversedY = false;
				break;
			case ZLViewWidget.Angle.DEGREES90:
				myReversedX = true;
				myReversedY = false;
				break;
			case ZLViewWidget.Angle.DEGREES180:
				myReversedX = true;
				myReversedY = true;
				break;
			case ZLViewWidget.Angle.DEGREES270:
				myReversedX = false;
				myReversedY = true;
				break;
		}
	}

	public void clear(ZLColor color) {
		if (!areSameColors(myBackgroundColor, color)) {
			myBackgroundColor = new Color(color.Red, color.Green, color.Blue);
		}
		myGraphics.setColor(myBackgroundColor);
		myGraphics.fillRect(0, 0, getWidth(), getHeight());
		myGraphics.setColor(myColor);
		// TODO: implement
	}

	protected void setFontInternal(String family, int size, boolean bold, boolean italic) {
		if (myGraphics != null) {
			final int style = (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0);
			myGraphics.setFont(new Font(family, style, size));
		}
	}

	public void setColor(ZLColor color, int style) {
		// TODO: use style
		if (!areSameColors(myColor, color)) {
			myColor = new Color(color.Red, color.Green, color.Blue);
			myGraphics.setColor(myColor);
		}
	}

	public void setFillColor(ZLColor color, int style) {
		// TODO: use style
		if (!areSameColors(myFillColor, color)) {
			myFillColor = new Color(color.Red, color.Green, color.Blue);
		}
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
	
	public int getStringWidth(char[] string, int offset, int length) {
		return myGraphics.getFontMetrics().charsWidth(string, offset, length);
	}

	protected int getSpaceWidthInternal() {
		return myGraphics.getFontMetrics().charWidth(' ');
	}
	protected int getStringHeightInternal() {
		return myGraphics.getFontMetrics().getHeight();
	}
	protected int getDescentInternal() {
		return myGraphics.getFontMetrics().getDescent();
	}
	public void drawString(int x, int y, char[] string, int offset, int length) {
		myGraphics.drawChars(string, offset, length, x, y);
	}

	public int imageWidth(ZLImageData imageData) {
		BufferedImage awtImage = ((ZLSwingImageData)imageData).getAwtImage();
		return (awtImage != null) ? awtImage.getWidth() : 0;
	}

	public int imageHeight(ZLImageData imageData) {
		BufferedImage awtImage = ((ZLSwingImageData)imageData).getAwtImage();
		return (awtImage != null) ? awtImage.getHeight() : 0;
	}

	public void drawImage(int x, int y, ZLImageData imageData) {
		BufferedImage awtImage = ((ZLSwingImageData)imageData).getAwtImage();
		if (awtImage != null) {
			myGraphics.drawImage(awtImage, x, y - awtImage.getHeight(), null);
		} 
	}

	public void drawLine(int x0, int y0, int x1, int y1) {
		if (myReversedX) {
			++x0;
			++x1;
		}
		if (myReversedY) {
			++y0;
			++y1;
		}
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
		myGraphics.setColor(myFillColor);
		myGraphics.fillRect(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
		myGraphics.setColor(myColor);
	}

	public void drawFilledCircle(int x, int y, int r) {
		// TODO: implement
	}

	void setGraphics(Graphics2D g) {
		myGraphics = g;
		resetFont();
	}
	//why?
	public String realFontFamilyName(String fontFamily) {
		return fontFamily;
	}
	
	protected void fillFamiliesList(ArrayList families) {
		String[] fontlist = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for (int i = 0; i < fontlist.length; i++) {
			families.add(fontlist[i]);
		}
		//Collections.sort(families);
	}

}
