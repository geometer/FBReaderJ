/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.android.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.*;

import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;

public final class ZLAndroidPaintContext extends ZLPaintContext {
	private Canvas myCanvas;
	private final Paint myPaint;
	private ZLColor myColor = new ZLColor(0, 0, 0);
	private ZLColor myFillColor = new ZLColor(0, 0, 0);

	private int myWidth;
	private int myHeight;
	private boolean myReversedX;
	private boolean myReversedY;

	static ZLAndroidPaintContext Instance() {
		if (ourInstance == null) {
			ourInstance = new ZLAndroidPaintContext();
		}
		return ourInstance;
	}

	private static ZLAndroidPaintContext ourInstance;

	private HashMap<String,Typeface[]> myTypefaces = new HashMap<String,Typeface[]>();

	private ZLAndroidPaintContext() {
		myPaint = new Paint();
		myPaint.setLinearText(false);
		myPaint.setAntiAlias(true);
		myPaint.setSubpixelText(false);
	}

	void setSize(int w, int h) {
		myWidth = w;
		myHeight = h;
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

	void beginPaint(Canvas canvas) {
		myCanvas = canvas;
		resetFont();
	}

	void endPaint() {
		myCanvas = null;
	}

	public void clear(ZLColor color) {
		// TODO: implement
		myColor = color;
		myPaint.setColor(Color.rgb(color.Red, color.Green, color.Blue));
		myCanvas.drawRect(0, 0, myWidth, myHeight, myPaint);
	}

	protected void setFontInternal(String family, int size, boolean bold, boolean italic) {
		final int style = (bold ? Typeface.BOLD : 0) | (italic ? Typeface.ITALIC : 0);
		final Paint paint = myPaint;
		Typeface[] typefaces = myTypefaces.get(family);
		if (typefaces == null) {
			typefaces = new Typeface[4];
			myTypefaces.put(family, typefaces);
		}
		Typeface typeface = typefaces[style];
		if (typeface == null) {
			typeface = Typeface.create(family, style);
			typefaces[style] = typeface;
		}
		paint.setTypeface(typeface);
		paint.setTextSize(size);
	}

	public void setColor(ZLColor color, int style) {
		// TODO: use style
		if (!myColor.equals(color)) {
			myColor = color;
			myPaint.setColor(Color.rgb(color.Red, color.Green, color.Blue));
		}
	}

	public void setFillColor(ZLColor color, int style) {
		// TODO: use style
		myFillColor = color;
	}

	public int getWidth() {
		return myWidth;
	}
	public int getHeight() {
		return myHeight;
	}
	
	public int getStringWidth(char[] string, int offset, int length) {
		return (int)(myPaint.measureText(string, offset, length) + 0.5f);
	}
	protected int getSpaceWidthInternal() {
		return (int)(myPaint.measureText(" ", 0, 1) + 0.5f);
	}
	protected int getStringHeightInternal() {
		return (int)(myPaint.getTextSize() + 0.5f);
	}
	protected int getDescentInternal() {
		return (int)(myPaint.descent() + 0.5f);
	}
	public void drawString(int x, int y, char[] string, int offset, int length) {
		myCanvas.drawText(string, offset, length, x, y, myPaint);
	}

	public int imageWidth(ZLImageData imageData) {
		Bitmap bitmap = ((ZLAndroidImageData)imageData).getBitmap();
		return (bitmap != null) ? bitmap.getWidth() : 0;
	}

	public int imageHeight(ZLImageData imageData) {
		Bitmap bitmap = ((ZLAndroidImageData)imageData).getBitmap();
		return (bitmap != null) ? bitmap.getHeight() : 0;
	}

	public void drawImage(int x, int y, ZLImageData imageData) {
		Bitmap bitmap = ((ZLAndroidImageData)imageData).getBitmap();
		if (bitmap != null) {
			myCanvas.drawBitmap(bitmap, x, y - bitmap.getHeight(), myPaint);
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
		final Paint paint = myPaint;
		final Canvas canvas = myCanvas;
		paint.setAntiAlias(false);
		canvas.drawLine(x0, y0, x1, y1, paint);
		canvas.drawPoint(x0, y0, paint);
		canvas.drawPoint(x1, y1, paint);
		paint.setAntiAlias(true);
	}

	public void fillRectangle(int x0, int y0, int x1, int y1) {
		ZLColor color = myFillColor;
		myPaint.setColor(Color.rgb(color.Red, color.Green, color.Blue));
		if (x1 < x0) {
			int swap = x1;
			x1 = x0;
			x0 = swap;
		}
		if (y1 < y0) {
			int swap = y1;
			y1 = y0;
			y0 = swap;
		}
		myCanvas.drawRect(x0, y0, x1 + 1, y1 + 1, myPaint);
		color = myColor;
		myPaint.setColor(Color.rgb(color.Red, color.Green, color.Blue));
	}
	public void drawFilledCircle(int x, int y, int r) {
		// TODO: implement
	}

	public String realFontFamilyName(String fontFamily) {
		// TODO: implement
		if ("Serif".equals(fontFamily)) {
			return fontFamily;
		}
		return "Sans";
	}
	
	protected void fillFamiliesList(ArrayList families) {
		// TODO: implement
		families.add("Sans");
		families.add("Serif");
	}
}
