package org.zlibrary.ui.swing.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.core.options.util.ZLColor;
import org.zlibrary.core.view.ZLPaintContext;

public final class ZLSwingPaintContext extends ZLPaintContext {
	public void clear(ZLColor color) {
		// TODO: implement
	}

	protected void setFontInternal(String family, int size, boolean bold, boolean italic) {
		if (myGraphics != null) {
			final int style = (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0);
			myGraphics.setFont(new Font(family, style, size));
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
	
	public int getStringWidth(String string, int offset, int length) {
		char data[] = new char[length];
		string.getChars(offset, offset + length, data, 0);
		return myGraphics.getFontMetrics().charsWidth(data, 0, length);
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
	public void drawString(int x, int y, String string, int offset, int length) {
		myGraphics.drawString(string.substring(offset, offset + length), x, y);
	}

	public int imageWidth(ZLImage image) {
		try {
			return ImageIO.read(new ByteArrayInputStream(image.byteData())).getWidth();
		} catch (IOException e) {
		} 
		return 0;
	}

	public int imageHeight(ZLImage image) {
		try {
			return ImageIO.read(new ByteArrayInputStream(image.byteData())).getHeight();
		} catch (IOException e) {
		} 
		return 0;
	}

	public void drawImage(int x, int y, ZLImage image) {
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(image.byteData()));
			myGraphics.drawImage(img, x, y - img.getHeight(), null);
		} catch (IOException e) {
		} 
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
		resetFont();
	}

	private Graphics2D myGraphics;
	private int myWidth;
	private int myHeight;
}
