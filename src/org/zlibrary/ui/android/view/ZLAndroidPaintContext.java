package org.zlibrary.ui.android.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import org.zlibrary.core.options.util.ZLColor;
import org.zlibrary.core.view.ZLPaintContext;

public final class ZLAndroidPaintContext extends ZLPaintContext {
	private Canvas myCanvas;
	private final Paint myPaint;

	private int myWidth;
	private int myHeight;

	private float[] myWidthsArray = new float[10];

	ZLAndroidPaintContext() {
		myPaint = new Paint();
		myPaint.setLinearText(true);
		myPaint.setAntiAlias(true);
	}

	void setSize(int w, int h) {
		myWidth = w;
		myHeight = h;
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
	}

	protected void setFontInternal(String family, int size, boolean bold, boolean italic) {
		// TODO: optimize
		final int style = (bold ? Typeface.BOLD : 0) | (italic ? Typeface.ITALIC : 0);
		//if (family == null) {
			family = "DroidSerif";
		//}
		myPaint.setTypeface(Typeface.create(family, style));
		myPaint.setTextSize(size);
	}

	public void setColor(ZLColor color, LineStyle style) {
		// TODO: implement
	}

	public void setFillColor(ZLColor color, FillStyle style) {
		// TODO: implement
	}

	public int getWidth() {
		return myWidth;
	}
	public int getHeight() {
		return myHeight;
	}
	
	public int getStringWidth(String string, int offset, int length) {
		float[] widths = myWidthsArray;
		if (widths.length < length) {
			widths = new float[length];
			myWidthsArray = widths;
		}
		myPaint.getTextWidths(string, offset, offset + length, widths);
		float sum = 0.5f;
		for (int i = 0; i < length; ++i) {
			sum += widths[i];
		}
		return (int)sum;
	}
	protected int getSpaceWidthInternal() {
		myPaint.getTextWidths(" ", 0, 1, myWidthsArray);
		return (int)(myWidthsArray[0] + 0.5f);
	}
	protected int getStringHeightInternal() {
		return (int)(myPaint.getTextSize() + 0.5f);
	}
	protected int getDescentInternal() {
		return (int)(myPaint.descent() + 0.5f);
	}
	public void drawString(int x, int y, String string, int offset, int length) {
		myCanvas.drawText(string, offset, offset + length, x, y, myPaint);
	}

	//int imageWidth(ZLImageData &image);
	//int imageHeight(ZLImageData &image);
	//public void drawImage(int x, int y, ZLImageData &image);

	public void drawLine(int x0, int y0, int x1, int y1) {
		myCanvas.drawLine(x0, y0, x1, y1, myPaint);
	}

	public void fillRectangle(int x0, int y0, int x1, int y1) {
		// TODO: implement
	}
	public void drawFilledCircle(int x, int y, int r) {
		// TODO: implement
	}

/*	public List<String> fontFamilies() {
		if (myFamilies.isEmpty()) {
			fillFamiliesList(myFamilies);
		}
		return myFamilies;
	}
	
	public String realFontFamilyName(String fontFamily);
	protected void fillFamiliesList(List<String> families);
*/
}
