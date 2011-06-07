/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.view;

import java.util.*;

import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImageData;

final class DummyPaintContext extends ZLPaintContext {
	DummyPaintContext() {
	}

	public void clear(ZLFile wallpaperFile, boolean doMirror) {
	}

	public void clear(ZLColor color) {
	}

	public ZLColor getBackgroundColor() {
		return new ZLColor(0, 0, 0);
	}

	protected void setFontInternal(String family, int size, boolean bold, boolean italic, boolean underline) {
	}

	public void setTextColor(ZLColor color) {
	}

	public void setLineColor(ZLColor color, int style) {
	}
	public void setLineWidth(int width) {
	}

	public void setFillColor(ZLColor color, int alpha, int style) {
	}

	public int getWidth() {
		return 1;
	}
	public int getHeight() {
		return 1;
	}
	
	public int getStringWidth(char[] string, int offset, int length) {
		return 1;
	}

	protected int getSpaceWidthInternal() {
		return 1;
	}

	protected int getStringHeightInternal() {
		return 1;
	}

	protected int getDescentInternal() {
		return 1;
	}

	public void drawString(int x, int y, char[] string, int offset, int length) {
	}

	public int imageWidth(ZLImageData image) {
		return 1;
	}
	public int imageHeight(ZLImageData image) {
		return 1;
	}
	public void drawImage(int x, int y, ZLImageData image) {
	}

	public void drawLine(int x0, int y0, int x1, int y1) {
	}
	public void fillRectangle(int x0, int y0, int x1, int y1) {
	}
	public void drawFilledCircle(int x, int y, int r) {
	}

	public void fillPolygon(int[] xs, int ys[]) {
	}
	public void drawPolygonalLine(int[] xs, int ys[]) {
	}
	public void drawOutline(int[] xs, int ys[]) {
	}

	public String realFontFamilyName(String fontFamily) {
		return fontFamily;
	}
	protected void fillFamiliesList(ArrayList<String> families) {
	}
}
