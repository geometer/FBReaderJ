package org.zlibrary.core.view;

import java.util.LinkedList;
import java.util.List;

import org.zlibrary.core.options.util.ZLColor;

abstract public class ZLPaintContext {
	private List<String> myFamilies = new LinkedList<String>();

	public enum LineStyle {
		SOLID_LINE,
		DASH_LINE,
	};

	public enum FillStyle {
		SOLID_FILL,
		HALF_FILL,
	};
	
	protected ZLPaintContext() {
	}

	abstract public void clear(ZLColor color);

	abstract public void setFont(String family, int size, boolean bold, boolean italic);

	final public void setColor(ZLColor color) {
		setColor(color, LineStyle.SOLID_LINE);
	}
	abstract public void setColor(ZLColor color, LineStyle style);

	final public void setFillColor(ZLColor color) {
		setFillColor(color, FillStyle.SOLID_FILL);
	}
	abstract public void setFillColor(ZLColor color, FillStyle style);

	abstract public int getWidth();
	abstract public int getHeight();
	
	abstract public int getStringWidth(String string/*, int len*/);
	abstract public int getSpaceWidth();
	abstract public int getStringHeight();
	abstract public int getDescent();
	abstract public void drawString(int x, int y, String string/*, int len*/);

	//int imageWidth(ZLImageData &image);
	//int imageHeight(ZLImageData &image);
	//abstract public void drawImage(int x, int y, ZLImageData &image);

	abstract public void drawLine(int x0, int y0, int x1, int y1);
	abstract public void fillRectangle(int x0, int y0, int x1, int y1);
	abstract public void drawFilledCircle(int x, int y, int r);

/*	public List<String> fontFamilies() {
		if (myFamilies.isEmpty()) {
			fillFamiliesList(myFamilies);
		}
		return myFamilies;
	}
	
	abstract public String realFontFamilyName(String fontFamily);
	abstract protected void fillFamiliesList(List<String> families);
*/
}


