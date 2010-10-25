/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

public final class ZLAndroidPaintContext extends ZLPaintContext {
	private final Canvas myCanvas;
	private final Paint myTextPaint = new Paint();
	private final Paint myLinePaint = new Paint();
	private final Paint myFillPaint = new Paint();
	private final Paint myOutlinePaint = new Paint();

	private final int myWidth;
	private final int myHeight;
	private final int myScrollbarWidth;

	private HashMap<String,Typeface[]> myTypefaces = new HashMap<String,Typeface[]>();

	ZLAndroidPaintContext(Canvas canvas, int width, int height, int scrollbarWidth) {
		myCanvas = canvas;
		myWidth = width - scrollbarWidth;
		myHeight = height;
		myScrollbarWidth = scrollbarWidth;

		myTextPaint.setLinearText(false);
		myTextPaint.setAntiAlias(true);
		myTextPaint.setSubpixelText(false);

		myOutlinePaint.setColor(Color.rgb(255, 127, 0));
		myOutlinePaint.setAntiAlias(true);
		myOutlinePaint.setDither(true);
		myOutlinePaint.setStrokeWidth(4);
		myOutlinePaint.setStyle(Paint.Style.STROKE);
		myOutlinePaint.setPathEffect(new CornerPathEffect(5));
		myOutlinePaint.setMaskFilter(new EmbossMaskFilter(new float[] {1, 1, 1}, .4f, 6f, 3.5f));
	}

	public void clear(ZLColor color) {
		myFillPaint.setColor(ZLAndroidColorUtil.rgb(color));
		myCanvas.drawRect(0, 0, myWidth + myScrollbarWidth, myHeight, myFillPaint);
	}

	public void drawOutline(int[] xs, int ys[]) {
		final int last = xs.length - 1;
		int xStart = (xs[0] + xs[last]) / 2;
		int yStart = (ys[0] + ys[last]) / 2;
		int xEnd = xStart;
		int yEnd = yStart;
		if (xs[0] != xs[last]) {
			if (xs[0] > xs[last]) {
				xStart -= 5;
				xEnd += 5;
			} else {
				xStart += 5;
				xEnd -= 5;
			}
		} else {
			if (ys[0] > ys[last]) {
				yStart -= 5;
				yEnd += 5;
			} else {
				yStart += 5;
				yEnd -= 5;
			}
		}

		final Path path = new Path();
		path.moveTo(xStart, yStart);
		for (int i = 0; i <= last; ++i) {
			path.lineTo(xs[i], ys[i]);
		}
		path.lineTo(xEnd, yEnd);
		myCanvas.drawPath(path, myOutlinePaint);
	}

	protected void setFontInternal(String family, int size, boolean bold, boolean italic, boolean underline) {
		final int style = (bold ? Typeface.BOLD : 0) | (italic ? Typeface.ITALIC : 0);
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
		myTextPaint.setTypeface(typeface);
		myTextPaint.setTextSize(size);
		myTextPaint.setUnderlineText(underline);
	}

	public void setTextColor(ZLColor color) {
		myTextPaint.setColor(ZLAndroidColorUtil.rgb(color));
	}

	public void setLineColor(ZLColor color, int style) {
		// TODO: use style
		myLinePaint.setColor(ZLAndroidColorUtil.rgb(color));
	}
	public void setLineWidth(int width) {
		myLinePaint.setStrokeWidth(width);
	}

	public void setFillColor(ZLColor color, int style) {
		// TODO: use style
		myFillPaint.setColor(ZLAndroidColorUtil.rgb(color));
	}

	public int getWidth() {
		return myWidth;
	}
	public int getHeight() {
		return myHeight;
	}
	
	public int getStringWidth(char[] string, int offset, int length) {
		return (int)(myTextPaint.measureText(string, offset, length) + 0.5f);
	}
	protected int getSpaceWidthInternal() {
		return (int)(myTextPaint.measureText(" ", 0, 1) + 0.5f);
	}
	protected int getStringHeightInternal() {
		return (int)(myTextPaint.getTextSize() + 0.5f);
	}
	protected int getDescentInternal() {
		return (int)(myTextPaint.descent() + 0.5f);
	}
	public void drawString(int x, int y, char[] string, int offset, int length) {
		myCanvas.drawText(string, offset, length, x, y, myTextPaint);
	}

	public int imageWidth(ZLImageData imageData) {
		Bitmap bitmap = ((ZLAndroidImageData)imageData).getBitmap(myWidth, myHeight);
		return ((bitmap != null) && !bitmap.isRecycled()) ? bitmap.getWidth() : 0;
	}

	public int imageHeight(ZLImageData imageData) {
		Bitmap bitmap = ((ZLAndroidImageData)imageData).getBitmap(myWidth, myHeight);
		return ((bitmap != null) && !bitmap.isRecycled())  ? bitmap.getHeight() : 0;
	}

	public void drawImage(int x, int y, ZLImageData imageData) {
		Bitmap bitmap = ((ZLAndroidImageData)imageData).getBitmap(myWidth, myHeight);
		if ((bitmap != null) && !bitmap.isRecycled()) {
			myCanvas.drawBitmap(bitmap, x, y - bitmap.getHeight(), myFillPaint);
		}
	}

	public void drawLine(int x0, int y0, int x1, int y1) {
		final Canvas canvas = myCanvas;
		final Paint paint = myLinePaint;
		paint.setAntiAlias(false);
		canvas.drawLine(x0, y0, x1, y1, paint);
		canvas.drawPoint(x0, y0, paint);
		canvas.drawPoint(x1, y1, paint);
		paint.setAntiAlias(true);
	}

	public void fillRectangle(int x0, int y0, int x1, int y1) {
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
		myCanvas.drawRect(x0, y0, x1 + 1, y1 + 1, myFillPaint);
	}
	public void drawFilledCircle(int x, int y, int r) {
		// TODO: implement
	}

	public String realFontFamilyName(String fontFamily) {
		// TODO: implement
		if ("Serif".equals(fontFamily)) {
			return "serif";
		}
		if ("sans-serif".equals(fontFamily)
				|| "serif".equals(fontFamily)
				|| "monospace".equals(fontFamily)) {
			return fontFamily;
		}
		return "sans-serif";
	}

	protected void fillFamiliesList(ArrayList<String> families) {
		// TODO: implement
		families.add("sans-serif");
		families.add("serif");
		families.add("monospace");
	}
}
