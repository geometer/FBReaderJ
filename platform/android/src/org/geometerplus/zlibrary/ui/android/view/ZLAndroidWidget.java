/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.view.*;
import android.util.AttributeSet;

import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import org.geometerplus.zlibrary.ui.android.util.ZLAndroidKeyUtil;

public class ZLAndroidWidget extends View {
	private ZLAndroidViewWidget myViewWidget;
	private Bitmap myMainBitmap;
	private Bitmap mySecondaryBitmap;
	private boolean mySecondaryBitmapIsUpToDate;
	private boolean myScrollingInProgress;
	private int myScrollingShift;
	private float myScrollingSpeed;
	private int myScrollingBound;

	public ZLAndroidWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ZLAndroidWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ZLAndroidWidget(Context context) {
		super(context);
	}

	public ZLAndroidPaintContext getPaintContext() {
		return ZLAndroidPaintContext.Instance();
	}

	void setViewWidget(ZLAndroidViewWidget viewWidget) {
		myViewWidget = viewWidget;
	}

	public void onDraw(final Canvas canvas) {
		super.onDraw(canvas);

		if (myScrollingInProgress || (myScrollingShift != 0)) {
			onDrawInScrolling(canvas);
		} else {
			onDrawStatic(canvas);
		}
	}

	private void onDrawInScrolling(Canvas canvas) {
		final int w = getWidth();
		final int h = getHeight();
		final ZLAndroidPaintContext context = ZLAndroidPaintContext.Instance();

		boolean stopScrolling = false;
		if (myScrollingInProgress) {
			myScrollingShift += (int)myScrollingSpeed;
			if (myScrollingSpeed > 0) {
				if (myScrollingShift >= myScrollingBound) {
					myScrollingShift = myScrollingBound;
					stopScrolling = true;
				}
			} else {
				if (myScrollingShift <= myScrollingBound) {
					myScrollingShift = myScrollingBound;
					stopScrolling = true;
				}
			}
			myScrollingSpeed *= 1.5;
		}
		canvas.drawBitmap(myMainBitmap, 0, myScrollingShift, context.Paint);
		int shift = (myScrollingShift < 0) ? (myScrollingShift + h) : (myScrollingShift - h);
		canvas.drawBitmap(mySecondaryBitmap, 0, shift, context.Paint);
		if (stopScrolling) {
			if (myScrollingBound != 0) {
				Bitmap swap = myMainBitmap;
				myMainBitmap = mySecondaryBitmap;
				mySecondaryBitmap = swap;
			}
			mySecondaryBitmapIsUpToDate = false;
			myScrollingInProgress = false;
			myScrollingShift = 0;
		} else {
			if (shift < 0) {
				shift += h;
			}
			canvas.drawLine(0, shift, w, shift, context.Paint);
			if (myScrollingInProgress) {
				postInvalidate();
			}
		}
	}

	void scrollTo(int shift) {
		if (myMainBitmap == null) {
			return;
		}
		drawOnBitmap(mySecondaryBitmap);
		myScrollingShift = shift;
		postInvalidate();
	}

	void startAutoScrolling(boolean forward) {
		if (myMainBitmap == null) {
			return;
		}
		drawOnBitmap(mySecondaryBitmap);
		myScrollingInProgress = true;
		final boolean scrollUp = myScrollingShift > 0;
		myScrollingSpeed = (scrollUp == forward) ? 3 : -3;
		final int h = getHeight();
		myScrollingBound = forward ? (scrollUp ? h : -h) : 0;
		postInvalidate();
	}

	private void drawOnBitmap(Bitmap bitmap) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if ((myViewWidget == null) || (view == null)) {
			return;
		}

		if (bitmap == myMainBitmap) {
			mySecondaryBitmapIsUpToDate = false;
		} else if (mySecondaryBitmapIsUpToDate) {
			return;
		} else {
			mySecondaryBitmapIsUpToDate = true;
		}

		final int w = getWidth();
		final int h = getHeight();
		final ZLAndroidPaintContext context = ZLAndroidPaintContext.Instance();

		Canvas canvas = new Canvas(bitmap);
		context.beginPaint(canvas);
		final int rotation = myViewWidget.getRotation();
		context.setRotation(rotation);
		final int scrollbarWidth = getVerticalScrollbarWidth();
		switch (rotation) {
			case ZLViewWidget.Angle.DEGREES0:
				context.setSize(w, h, scrollbarWidth);
				break;
			case ZLViewWidget.Angle.DEGREES90:
				context.setSize(h, w, scrollbarWidth);
				canvas.rotate(270, h / 2, h / 2);
				break;
			case ZLViewWidget.Angle.DEGREES180:
				context.setSize(w, h, scrollbarWidth);
				canvas.rotate(180, w / 2, h / 2);
				break;
			case ZLViewWidget.Angle.DEGREES270:
				context.setSize(h, w, scrollbarWidth);
				canvas.rotate(90, w / 2, w / 2);
				break;
		}
		int dy = (bitmap == myMainBitmap) ? 0 : ((myScrollingShift > 0) ? 1 : 0);
		view.paint(0, dy);
		context.endPaint();
	}

	private void onDrawStatic(Canvas canvas) {
		final int w = getWidth();
		final int h = getHeight();

		if ((myMainBitmap != null) && ((myMainBitmap.getWidth() != w) || (myMainBitmap.getHeight() != h))) {
			myMainBitmap = null;
		}
		if (myMainBitmap == null) {
			myMainBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			mySecondaryBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		}
		drawOnBitmap(myMainBitmap);
		canvas.drawBitmap(myMainBitmap, 0, 0, ZLAndroidPaintContext.Instance().Paint);
	}

	public boolean onTrackballEvent(MotionEvent event) {
		ZLApplication.Instance().getCurrentView().onTrackballRotated((int)(10 * event.getX()), (int)(10 * event.getY()));
		return true;
	}

	public boolean onTouchEvent(MotionEvent event) {
		int x = (int)event.getX();
		int y = (int)event.getY();
		switch (myViewWidget.getRotation()) {
			case ZLViewWidget.Angle.DEGREES0:
				break;
			case ZLViewWidget.Angle.DEGREES90:
			{
				int swap = x;
				x = getHeight() - y - 1;
				y = swap;
				break;
			}
			case ZLViewWidget.Angle.DEGREES180:
			{
				x = getWidth() - x - 1;
				y = getHeight() - y - 1;
				break;
			}
			case ZLViewWidget.Angle.DEGREES270:
			{
				int swap = getWidth() - x - 1;
				x = y;
				y = swap;
				break;
			}
		}

		ZLView view = ZLApplication.Instance().getCurrentView();
		switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				view.onStylusRelease(x, y);
				break;
			case MotionEvent.ACTION_DOWN:
				view.onStylusPress(x, y);
				break;
			case MotionEvent.ACTION_MOVE:
				view.onStylusMovePressed(x, y);
				break;
		}

		return true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		final String keyName = ZLAndroidKeyUtil.getKeyNameByCode(keyCode);
		if (keyName.equals("<Menu>") || keyName.equals("<Call>")) {
			return false;
		}
		ZLApplication.Instance().doActionByKey(keyName);
		return true;
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		final String keyName = ZLAndroidKeyUtil.getKeyNameByCode(keyCode);
		if (keyName.equals("<Menu>") || keyName.equals("<Call>")) {
			return false;
		}
		return true;
	}

	private int myScrollBarRange;
	private int myScrollBarOffset;
	private int myScrollBarThumbSize;

	void setVerticalScrollbarParameters(int full, int from, int to) {
		if (full < 0) {
			full = 0;
		}
		if (from < 0) {
			from = 0;
		} else if (from >= full) {
			from = full - 1;
		}
		if (to <= from) {
			to = from + 1;
		} else if (to > full) {
			to = full;
		}
		myScrollBarRange = full;
		myScrollBarOffset = from;
		myScrollBarThumbSize = to - from;
	}

	protected int computeVerticalScrollExtent() {
		return myScrollBarThumbSize;
	}

	protected int computeVerticalScrollOffset() {
		return myScrollBarOffset;
	}

	protected int computeVerticalScrollRange() {
		return myScrollBarRange;
	}
}
