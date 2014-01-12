/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

import java.util.*;

import android.graphics.*;
import android.util.FloatMath;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.view.ZLView;

abstract class AnimationProvider {
	static enum Mode {
		NoScrolling(false),
		ManualScrolling(false),
		AnimatedScrollingForward(true),
		AnimatedScrollingBackward(true);

		final boolean Auto;

		Mode(boolean auto) {
			Auto = auto;
		}
	}
	private Mode myMode = Mode.NoScrolling;

	private final BitmapManager myBitmapManager;
	protected int myStartX;
	protected int myStartY;
	protected int myEndX;
	protected int myEndY;
	protected ZLView.Direction myDirection;
	protected float mySpeed;

	protected int myWidth;
	protected int myHeight;

	protected AnimationProvider(BitmapManager bitmapManager) {
		myBitmapManager = bitmapManager;
	}

	Mode getMode() {
		return myMode;
	}

	final void terminate() {
		myMode = Mode.NoScrolling;
		mySpeed = 0;
		myDrawInfos.clear();
	}

	final void startManualScrolling(int x, int y) {
		if (!myMode.Auto) {
			myMode = Mode.ManualScrolling;
			myEndX = myStartX = x;
			myEndY = myStartY = y;
		}
	}

	void scrollTo(int x, int y) {
		if (myMode == Mode.ManualScrolling) {
			myEndX = x;
			myEndY = y;
		}
	}

	void startAnimatedScrolling(int x, int y, int speed) {
		if (myMode != Mode.ManualScrolling) {
			return;
		}

		if (getPageToScrollTo(x, y) == ZLView.PageIndex.current) {
			return;
		}

		final int diff = myDirection.IsHorizontal ? x - myStartX : y - myStartY;
		final int dpi = ZLibrary.Instance().getDisplayDPI();
		final int minDiff = myDirection.IsHorizontal ?
			(myWidth > myHeight ? myWidth / 4 : myWidth / 3) :
			(myHeight > myWidth ? myHeight / 4 : myHeight / 3);
		boolean forward = Math.abs(diff) > Math.min(minDiff, dpi / 2);

		myMode = forward ? Mode.AnimatedScrollingForward : Mode.AnimatedScrollingBackward;

		float velocity = 15;
		if (myDrawInfos.size() > 1) {
			int duration = 0;
			for (DrawInfo info : myDrawInfos) {
				duration += info.Duration;
			}
			duration /= myDrawInfos.size();
			final long time = System.currentTimeMillis();
			myDrawInfos.add(new DrawInfo(x, y, time, time + duration));
			velocity = 0;
			for (int i = 1; i < myDrawInfos.size(); ++i) {
				final DrawInfo info0 = myDrawInfos.get(i - 1);
				final DrawInfo info1 = myDrawInfos.get(i);
				final float dX = info0.X - info1.X;
				final float dY = info0.Y - info1.Y;
				velocity += FloatMath.sqrt(dX * dX + dY * dY) / Math.max(1, info1.Start - info0.Start);
			}
			velocity /= myDrawInfos.size() - 1;
			velocity *= duration;
			velocity = Math.min(100, Math.max(15, velocity));
		}
		myDrawInfos.clear();

		if (getPageToScrollTo() == ZLView.PageIndex.previous) {
			forward = !forward;
		}

		switch (myDirection) {
			case up:
			case rightToLeft:
				mySpeed = forward ? -velocity : velocity;
				break;
			case leftToRight:
			case down:
				mySpeed = forward ? velocity : -velocity;
				break;
		}

		startAnimatedScrollingInternal(speed);
	}

	public void startAnimatedScrolling(ZLView.PageIndex pageIndex, Integer x, Integer y, int speed) {
		if (myMode.Auto) {
			return;
		}

		terminate();
		myMode = Mode.AnimatedScrollingForward;

		switch (myDirection) {
			case up:
			case rightToLeft:
				mySpeed = pageIndex == ZLView.PageIndex.next ? -15 : 15;
				break;
			case leftToRight:
			case down:
				mySpeed = pageIndex == ZLView.PageIndex.next ? 15 : -15;
				break;
		}
		setupAnimatedScrollingStart(x, y);
		startAnimatedScrollingInternal(speed);
	}

	protected abstract void startAnimatedScrollingInternal(int speed);
	protected abstract void setupAnimatedScrollingStart(Integer x, Integer y);

	boolean inProgress() {
		return myMode != Mode.NoScrolling;
	}

	protected int getScrollingShift() {
		return myDirection.IsHorizontal ? myEndX - myStartX : myEndY - myStartY;
	}

	final void setup(ZLView.Direction direction, int width, int height) {
		myDirection = direction;
		myWidth = width;
		myHeight = height;
	}

	abstract void doStep();

	int getScrolledPercent() {
		final int full = myDirection.IsHorizontal ? myWidth : myHeight;
		final int shift = Math.abs(getScrollingShift());
		return 100 * shift / full;
	}

	static class DrawInfo {
		final int X, Y;
		final long Start;
		final int Duration;

		DrawInfo(int x, int y, long start, long finish) {
			X = x;
			Y = y;
			Start = start;
			Duration = (int)(finish - start);
		}
	}
	final private List<DrawInfo> myDrawInfos = new LinkedList<DrawInfo>();

	final void draw(Canvas canvas) {
		myBitmapManager.setSize(myWidth, myHeight);
		final long start = System.currentTimeMillis();
		drawInternal(canvas);
		myDrawInfos.add(new DrawInfo(myEndX, myEndY, start, System.currentTimeMillis()));
		if (myDrawInfos.size() > 3) {
			myDrawInfos.remove(0);
		}
	}

	protected abstract void drawInternal(Canvas canvas);

	abstract ZLView.PageIndex getPageToScrollTo(int x, int y);

	final ZLView.PageIndex getPageToScrollTo() {
		return getPageToScrollTo(myEndX, myEndY);
	}

	protected Bitmap getBitmapFrom() {
		return myBitmapManager.getBitmap(ZLView.PageIndex.current);
	}

	protected Bitmap getBitmapTo() {
		return myBitmapManager.getBitmap(getPageToScrollTo());
	}
}
