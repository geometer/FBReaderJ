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

package org.geometerplus.zlibrary.ui.android.view;

import android.graphics.*;

import org.geometerplus.zlibrary.core.view.ZLView;

abstract class AnimationProvider {
	static enum ScrollingMode {
		NoScrolling(false),
		ManualScrolling(false),
		AutoScrollingForward(true),
		AutoScrollingBackward(true);

		final boolean Auto;

		ScrollingMode(boolean auto) {
			Auto = auto;
		}
	}
	private ScrollingMode myScrollingMode = ScrollingMode.NoScrolling;
	
	protected final Paint myPaint;
	protected int myStartX;
	protected int myStartY;
	protected int myEndX;
	protected int myEndY;
	protected ZLView.Direction myDirection;

	protected int myWidth;
	protected int myHeight;

	protected AnimationProvider(Paint paint) {
		myPaint = paint;
	}

	ScrollingMode getScrollingMode() {
		return myScrollingMode;
	}

	void setScrollingMode(ScrollingMode state) {
		myScrollingMode = state;
	}

	void terminate() {
		myScrollingMode = ScrollingMode.NoScrolling;
	}

	boolean inProgress() {
		return myScrollingMode != ScrollingMode.NoScrolling;
	}

	int getScrollingShift() {
		return myDirection.IsHorizontal ? myEndX - myStartX : myEndY - myStartY;
	}

	void setup(int startX, int startY, int endX, int endY, ZLView.Direction direction, int width, int height) {
		myStartX = startX;
		myStartY = startY;
		myEndX = endX;
		myEndY = endY;
		myDirection = direction;
		myWidth = width;
		myHeight = height;
	}

	abstract void draw(Canvas canvas, Bitmap bgBitmap, Bitmap fgBitmap);

	abstract ZLView.PageIndex getPageToScrollTo();
}
