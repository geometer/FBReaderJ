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
	static enum Mode {
		NoScrolling(false),
		ManualScrolling(false),
		AutoScrollingForward(true),
		AutoScrollingBackward(true);

		final boolean Auto;

		Mode(boolean auto) {
			Auto = auto;
		}
	}
	private Mode myMode = Mode.NoScrolling;
	
	protected final Paint myPaint;
	protected int myStartX;
	protected int myStartY;
	protected int myEndX;
	protected int myEndY;
	protected ZLView.Direction myDirection;

	protected int myWidth;
	protected int myHeight;

	private float mySpeed;

	protected AnimationProvider(Paint paint) {
		myPaint = paint;
	}

	Mode getMode() {
		return myMode;
	}

	void terminate() {
		myMode = Mode.NoScrolling;
		mySpeed = 0;
	}

	void startManualScrolling(int startX, int startY, int endX, int endY, ZLView.Direction direction, int w, int h) {
		myMode = Mode.ManualScrolling;
		setup(startX, startY, endX, endY, direction, w, h);
	}

	void startAutoScrolling(boolean forward, float speed, ZLView.Direction direction, int w, int h) {
		if (!inProgress()) {
			final int x, y;
			if (direction.IsHorizontal) {
				x = speed < 0 ? w : 0;
				y = 0;
			} else {
				x = 0;
				y = speed < 0 ? h : 0;
			}
			setup(x, y, x, y, direction, w, h);
		}

		myMode = forward
			? Mode.AutoScrollingForward
			: Mode.AutoScrollingBackward;
		mySpeed = speed;
	}

	boolean inProgress() {
		return myMode != Mode.NoScrolling;
	}

	private int getScrollingShift() {
		return myDirection.IsHorizontal ? myEndX - myStartX : myEndY - myStartY;
	}

	private void setup(int startX, int startY, int endX, int endY, ZLView.Direction direction, int width, int height) {
		myStartX = startX;
		myStartY = startY;
		myEndX = endX;
		myEndY = endY;
		myDirection = direction;
		myWidth = width;
		myHeight = height;
	}

	void doStep() {
		if (!myMode.Auto) {
			return;
		}

		switch (myDirection) {
			case leftToRight:
				myEndX -= (int)mySpeed;
				break;
			case rightToLeft:
				myEndX += (int)mySpeed;
				break;
			case up:
				myEndY += (int)mySpeed;
				break;
			case down:
				myEndY -= (int)mySpeed;
				break;
		}
		final int bound;
		if (myMode == Mode.AutoScrollingForward) {
			bound = myDirection.IsHorizontal ? myWidth : myHeight;
		} else {
			bound = 0;
		}
		if (mySpeed > 0) {
			if (getScrollingShift() >= bound) {
				if (myDirection.IsHorizontal) {
					myEndX = myStartX + bound;
				} else {
					myEndY = myStartY + bound;
				}
				terminate();
				return;
			}
		} else {
			if (getScrollingShift() <= -bound) {
				if (myDirection.IsHorizontal) {
					myEndX = myStartX - bound;
				} else {
					myEndY = myStartY - bound;
				}
				terminate();
				return;
			}
		}
		mySpeed *= 1.5;
	}

	int getScrolledPercent() {
		final int full = myDirection.IsHorizontal ? myWidth : myHeight;
		final int shift = Math.abs(getScrollingShift());
		return 100 * shift / full;
	}

	abstract void draw(Canvas canvas, Bitmap bgBitmap, Bitmap fgBitmap);

	abstract ZLView.PageIndex getPageToScrollTo();
}
