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

import java.util.*;

import android.graphics.*;
import android.util.FloatMath;

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
	protected float mySpeed;

	protected int myWidth;
	protected int myHeight;

	protected AnimationProvider(Paint paint) {
		myPaint = paint;
	}

	Mode getMode() {
		return myMode;
	}

	void terminate() {
		myMode = Mode.NoScrolling;
		mySpeed = 0;
		myDrawInfos.clear();
	}

	void startManualScrolling(int x, int y, ZLView.Direction direction, int w, int h) {
		myMode = Mode.ManualScrolling;
		setup(x, y, direction, w, h);
	}

	void scrollTo(int x, int y) {
		if (myMode == Mode.ManualScrolling) {
			myEndX = x;
			myEndY = y;
		}
	}

	final void startAutoScrolling(boolean forward, float startSpeed, ZLView.Direction direction, int w, int h, Integer x, Integer y, int speed) {
		if (myDrawInfos.size() <= 1) {
			startSpeed *= 5;
		} else {
			int duration = 0;
			for (DrawInfo info : myDrawInfos) {
				duration += info.Duration;
				System.err.println(info.X + ":" + info.Y + " :: " + info.Start + " " + info.Duration);
			}
			duration /= myDrawInfos.size();
			final long time = System.currentTimeMillis();
			myDrawInfos.add(new DrawInfo(myEndX, myEndY, time, time + duration));
			float velocity = 0;
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
			startSpeed = startSpeed > 0 ? velocity : -velocity;
		}
		System.err.println("startSpeed = " + startSpeed);
		myDrawInfos.clear();
		startAutoScrollingInternal(forward, startSpeed, direction, w, h, x, y, speed);
	}

	protected void startAutoScrollingInternal(boolean forward, float startSpeed, ZLView.Direction direction, int w, int h, Integer x, Integer y, int speed) {
		if (!inProgress()) {
			if (x == null || y == null) {
				if (direction.IsHorizontal) {
					x = speed < 0 ? w : 0;
					y = 0;
				} else {
					x = 0;
					y = speed < 0 ? h : 0;
				}
			}
			setup(x, y, direction, w, h);
		}

		myMode = forward
			? Mode.AutoScrollingForward
			: Mode.AutoScrollingBackward;
		mySpeed = startSpeed;
	}

	boolean inProgress() {
		return myMode != Mode.NoScrolling;
	}

	protected int getScrollingShift() {
		return myDirection.IsHorizontal ? myEndX - myStartX : myEndY - myStartY;
	}

	private void setup(int x, int y, ZLView.Direction direction, int width, int height) {
		myStartX = x;
		myStartY = y;
		myEndX = x;
		myEndY = y;
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

	final void draw(Canvas canvas, Bitmap bgBitmap, Bitmap fgBitmap) {
		final long start = System.currentTimeMillis();
		drawInternal(canvas, bgBitmap, fgBitmap);
		myDrawInfos.add(new DrawInfo(myEndX, myEndY, start, System.currentTimeMillis()));
		if (myDrawInfos.size() > 3) {
			myDrawInfos.remove(0);
		}
	}

	protected abstract void drawInternal(Canvas canvas, Bitmap bgBitmap, Bitmap fgBitmap);

	abstract ZLView.PageIndex getPageToScrollTo();
}
