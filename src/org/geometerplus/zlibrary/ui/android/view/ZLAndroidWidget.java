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

import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.util.AttributeSet;

import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivity;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidKeyUtil;

public class ZLAndroidWidget extends View implements View.OnLongClickListener {
	private final Paint myPaint = new Paint();
	private final Paint myEdgePaint = new Paint();
	private Bitmap myMainBitmap;
	private Bitmap mySecondaryBitmap;
	private boolean mySecondaryBitmapIsUpToDate;
	private Bitmap myFooterBitmap;

	private boolean myScrollingInProgress;
	private int myScrollingShift;
	private float myScrollingSpeed;
	private int myScrollingBound;

	public ZLAndroidWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ZLAndroidWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ZLAndroidWidget(Context context) {
		super(context);
		init();
	}

	private void init() {
		// next line prevent ignoring first onKeyDown DPad event
		// after any dialog was closed
		setFocusableInTouchMode(true);
		setDrawingCacheEnabled(false);
		setOnLongClickListener(this);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (myScreenIsTouched) {
			final ZLView view = ZLApplication.Instance().getCurrentView();
			myScrollingInProgress = false;
			myScrollingShift = 0;
			myScreenIsTouched = false;
			view.onScrollingFinished(ZLView.PageIndex.current);
			setPageToScrollTo(ZLView.PageIndex.current);
		}
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		final Context context = getContext();
		if (context instanceof ZLAndroidActivity) {
			((ZLAndroidActivity)context).createWakeLock();
		} else {
			System.err.println("A surprise: view's context is not a ZLAndroidActivity");
		}
		super.onDraw(canvas);

		final int w = getWidth();
		final int h = getMainAreaHeight();

		if (myMainBitmap != null &&
			(myMainBitmap.getWidth() != w || myMainBitmap.getHeight() != h)) {
			myMainBitmap = null;
			mySecondaryBitmap = null;
			System.gc();
			System.gc();
			System.gc();
		}
		if (myMainBitmap == null) {
			myMainBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
			mySecondaryBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
			mySecondaryBitmapIsUpToDate = false;
			drawOnBitmap(myMainBitmap);
		}

		if (myScrollingInProgress || myScrollingShift != 0) {
			onDrawInScrolling(canvas);
		} else {
			onDrawStatic(canvas);
			ZLApplication.Instance().onRepaintFinished();
		}
	}

	private void onDrawInScrolling(Canvas canvas) {
		final ZLView view = ZLApplication.Instance().getCurrentView();

		final int w = getWidth();
		final int h = getMainAreaHeight();

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
		final int size = myScrollHorizontally ? w : h;
		int shift = myScrollingShift < 0 ? myScrollingShift + size : myScrollingShift - size;
		switch (view.getAnimationType()) {
			case none:
				break;
			case shift:
				canvas.drawBitmap(
					mySecondaryBitmap,
					myScrollHorizontally ? shift : 0,
					myScrollHorizontally ? 0 : shift,
					myPaint
				);
				break;
			case slide:
			case curl:
				canvas.drawBitmap(
					mySecondaryBitmap,
					0, 0,
					myPaint
				);
				break;
		}
		switch (view.getAnimationType()) {
			case none:
				canvas.drawBitmap(
					myMainBitmap,
					0, 0,
					myPaint
				);
				break;
			case shift:
			case slide:
				canvas.drawBitmap(
					myMainBitmap,
					myScrollHorizontally ? myScrollingShift : 0,
					myScrollHorizontally ? 0 : myScrollingShift,
					myPaint
				);
				if (shift < 0) {
					shift += size;
				}
				// TODO: set color
				if (shift > 0 && shift < size) {
					myPaint.setColor(Color.rgb(127, 127, 127));
					if (myScrollHorizontally) {
						canvas.drawLine(shift, 0, shift, h + 1, myPaint);
					} else {
						canvas.drawLine(0, shift, w + 1, shift, myPaint);
					}
				}
				break;
			case curl:
			{
				final int x, y;
				if (myScrollHorizontally) {
					x = - myScrollingShift;
					y = x * h / w;
				} else {
					y = - myScrollingShift;
					x = y * w / h;
				}

				final Path fgPath = new Path();
				fgPath.moveTo(Math.max(0, w - x * 4 / 3), h);
				fgPath.lineTo(w - x, h - y);
				fgPath.lineTo(w, Math.max(0, h - y * 4 / 3));
				fgPath.lineTo(w, 0);
				fgPath.lineTo(0, 0);
				fgPath.lineTo(0, h);
				canvas.clipPath(fgPath);
				canvas.drawBitmap(
					myMainBitmap,
					0, 0,
					myPaint
				);
				canvas.restore();

				if (shift > 0 && shift < size) {
					myEdgePaint.setColor(ZLAndroidPaintContext.getFillColor());
					myEdgePaint.setAntiAlias(true);
					myEdgePaint.setStyle(Paint.Style.FILL);
					myEdgePaint.setShadowLayer(25, 5, 5, 0x99000000);

					final Path path = new Path();
					path.moveTo(Math.max(0, w - x * 4 / 3), h);
					path.lineTo(w - x, h - y);
					path.lineTo(w, Math.max(0, h - y * 4 / 3));
					canvas.drawPath(path, myEdgePaint);
				}

				break;
			}
		}

		if (stopScrolling) {
			if (myScrollingBound != 0) {
				Bitmap swap = myMainBitmap;
				myMainBitmap = mySecondaryBitmap;
				mySecondaryBitmap = swap;
				mySecondaryBitmapIsUpToDate = false;
				view.onScrollingFinished(myViewPageToScrollTo);
				ZLApplication.Instance().onRepaintFinished();
			} else {
				view.onScrollingFinished(ZLView.PageIndex.current);
			}
			setPageToScrollTo(ZLView.PageIndex.current);
			myScrollingInProgress = false;
			myScrollingShift = 0;
		} else {
			if (myScrollingInProgress) {
				postInvalidate();
			}
		}

		drawFooter(canvas);
	}

	private ZLView.PageIndex myViewPageToScrollTo = ZLView.PageIndex.current;
	private boolean myScrollHorizontally;
	private void setPageToScrollTo(ZLView.PageIndex viewPage) {
		if (myViewPageToScrollTo != viewPage) {
			myViewPageToScrollTo = viewPage;
			mySecondaryBitmapIsUpToDate = false;
		}
	}

	public void scrollManually(int startX, int startY, int endX, int endY, boolean horizontally) {
		myScrollHorizontally = horizontally;
		final int shift = horizontally ? endX - startX : endY - startY;

		if (myMainBitmap == null) {
			return;
		}
		if ((shift > 0 && myScrollingShift <= 0) ||
			(shift < 0 && myScrollingShift >= 0)) {
			mySecondaryBitmapIsUpToDate = false;
		}
		myScrollingShift = shift;
		setPageToScrollTo(shift < 0 ? ZLView.PageIndex.next : ZLView.PageIndex.previous);
		drawOnBitmap(mySecondaryBitmap);
		postInvalidate();
	}

	public void scrollToCenter() {
		if (myMainBitmap == null) {
			return;
		}
		myScrollingShift = 0;
		setPageToScrollTo(ZLView.PageIndex.current);
		drawOnBitmap(mySecondaryBitmap);
		postInvalidate();
	}

	public void startAutoScrolling(ZLView.PageIndex viewPage, boolean horizontally) {
		if (myMainBitmap == null) {
			return;
		}
		myScrollingInProgress = true;
		myScrollHorizontally = horizontally;
		switch (viewPage) {
			case current:
				switch (myViewPageToScrollTo) {
					case current:
						myScrollingSpeed = 0;
						break;
					case previous:
						myScrollingSpeed = -3;
						break;
					case next:
						myScrollingSpeed = 3;
						break;
				}
				myScrollingBound = 0;
				break;
			case previous:
				myScrollingSpeed = 3;
				myScrollingBound = horizontally ? getWidth() : getMainAreaHeight();
				setPageToScrollTo(viewPage);
				break;
			case next:
				myScrollingSpeed = -3;
				myScrollingBound = horizontally ? -getWidth() : -getMainAreaHeight();
				setPageToScrollTo(viewPage);
				break;
		}
		drawOnBitmap(mySecondaryBitmap);
		postInvalidate();
	}

	private void drawOnBitmap(Bitmap bitmap) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (view == null) {
			return;
		}

		if (bitmap == myMainBitmap) {
			mySecondaryBitmapIsUpToDate = false;
		} else if (mySecondaryBitmapIsUpToDate) {
			return;
		} else {
			mySecondaryBitmapIsUpToDate = true;
		}

		final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
			new Canvas(bitmap),
			getWidth(),
			getMainAreaHeight(),
			view.isScrollbarShown() ? getVerticalScrollbarWidth() : 0
		);
		view.paint(
			context,
			bitmap == myMainBitmap ? ZLView.PageIndex.current : myViewPageToScrollTo
		);
	}

	private void drawFooter(Canvas canvas) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		final ZLView.FooterArea footer = view.getFooterArea();
		if (footer != null) {
			if (myFooterBitmap != null &&
				(myFooterBitmap.getWidth() != getWidth() ||
				 myFooterBitmap.getHeight() != footer.getHeight())) {
				myFooterBitmap = null;
			}
			if (myFooterBitmap == null) {
				myFooterBitmap = Bitmap.createBitmap(getWidth(), footer.getHeight(), Bitmap.Config.RGB_565);
			}
			final ZLAndroidPaintContext context = new ZLAndroidPaintContext(
				new Canvas(myFooterBitmap),
				getWidth(),
				footer.getHeight(),
				view.isScrollbarShown() ? getVerticalScrollbarWidth() : 0
			);
			footer.paint(context);
			canvas.drawBitmap(myFooterBitmap, 0, getMainAreaHeight(), myPaint);
		} else {
			myFooterBitmap = null;
		}
	}

	private void onDrawStatic(Canvas canvas) {
		drawOnBitmap(myMainBitmap);
		canvas.drawBitmap(myMainBitmap, 0, 0, myPaint);
		drawFooter(canvas);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			onKeyDown(KeyEvent.KEYCODE_DPAD_CENTER, null);
		} else {
			ZLApplication.Instance().getCurrentView().onTrackballRotated((int)(10 * event.getX()), (int)(10 * event.getY()));
		}
		return true;
	}


	private class LongClickRunnable implements Runnable {
		public void run() {
			if (performLongClick()) {
				myLongClickPerformed = true;
			}
		}
	}
	private volatile LongClickRunnable myPendingLongClickRunnable;
	private volatile boolean myLongClickPerformed;

	private void postLongClickRunnable() {
        myLongClickPerformed = false;
		myPendingPress = false;
        if (myPendingLongClickRunnable == null) {
            myPendingLongClickRunnable = new LongClickRunnable();
        }
        postDelayed(myPendingLongClickRunnable, 2 * ViewConfiguration.getLongPressTimeout());
    }

	private class ShortClickRunnable implements Runnable {
		public void run() {
			final ZLView view = ZLApplication.Instance().getCurrentView();
			view.onFingerSingleTap(myPressedX, myPressedY);
			myPendingPress = false;
			myPendingShortClickRunnable = null;
		}
	}
	private volatile ShortClickRunnable myPendingShortClickRunnable;

	private volatile boolean myPendingPress;
	private volatile boolean myPendingDoubleTap;
	private int myPressedX, myPressedY;
	private boolean myScreenIsTouched;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int)event.getX();
		int y = (int)event.getY();

		final ZLView view = ZLApplication.Instance().getCurrentView();
		switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				if (myPendingDoubleTap) {
					view.onFingerDoubleTap(x, y);
				} if (myLongClickPerformed) {
					view.onFingerReleaseAfterLongPress(x, y);
				} else {
					if (myPendingLongClickRunnable != null) {
						removeCallbacks(myPendingLongClickRunnable);
						myPendingLongClickRunnable = null;
					}
					if (myPendingPress) {
						if (view.isDoubleTapSupported()) {
        					if (myPendingShortClickRunnable == null) {
            					myPendingShortClickRunnable = new ShortClickRunnable();
        					}
        					postDelayed(myPendingShortClickRunnable, ViewConfiguration.getDoubleTapTimeout());
						} else {
							view.onFingerSingleTap(x, y);
						}
					} else {
						view.onFingerRelease(x, y);
					}
				}
				myPendingDoubleTap = false;
				myPendingPress = false;
				myScreenIsTouched = false;
				break;
			case MotionEvent.ACTION_DOWN:
				if (myPendingShortClickRunnable != null) {
					removeCallbacks(myPendingShortClickRunnable);
					myPendingShortClickRunnable = null;
					myPendingDoubleTap = true;
				} else {
					postLongClickRunnable();
					myPendingPress = true;
				}
				myScreenIsTouched = true;
				myPressedX = x;
				myPressedY = y;
				break;
			case MotionEvent.ACTION_MOVE:
			{
				final int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
				final boolean isAMove =
					Math.abs(myPressedX - x) > slop || Math.abs(myPressedY - y) > slop;
				if (isAMove) {
					myPendingDoubleTap = false;
				}
				if (myLongClickPerformed) {
					view.onFingerMoveAfterLongPress(x, y);
				} else {
					if (myPendingPress) {
						if (isAMove) {
							if (myPendingShortClickRunnable != null) {
								removeCallbacks(myPendingShortClickRunnable);
								myPendingShortClickRunnable = null;
							}
							if (myPendingLongClickRunnable != null) {
								removeCallbacks(myPendingLongClickRunnable);
							}
							view.onFingerPress(myPressedX, myPressedY);
							myPendingPress = false;
						}
					}
					if (!myPendingPress) {
						view.onFingerMove(x, y);
					}
				}
				break;
			}
		}

		return true;
	}

	public boolean onLongClick(View v) {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		return view.onFingerLongPress(myPressedX, myPressedY);
	}

	private String myKeyUnderTracking;
	private long myTrackingStartTime;

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		final ZLApplication application = ZLApplication.Instance();

		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_DPAD_CENTER:
			{
				final String keyName = ZLAndroidKeyUtil.getKeyNameByCode(keyCode);
				if (myKeyUnderTracking != null) {
					if (myKeyUnderTracking.equals(keyName)) {
						return true;
					} else {
						myKeyUnderTracking = null;
					}
				}
				if (application.hasActionForKey(keyName, true)) {
					myKeyUnderTracking = keyName;
					myTrackingStartTime = System.currentTimeMillis();
					return true;
				} else {
					return application.doActionByKey(keyName, false);
				}
			}
			case KeyEvent.KEYCODE_DPAD_LEFT:
				application.getCurrentView().onTrackballRotated(-1, 0);
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				application.getCurrentView().onTrackballRotated(1, 0);
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				application.getCurrentView().onTrackballRotated(0, 1);
				return true;
			case KeyEvent.KEYCODE_DPAD_UP:
				application.getCurrentView().onTrackballRotated(0, -1);
				return true;
			default:
				return false;
		}
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_DPAD_CENTER:
				if (myKeyUnderTracking != null) {
					final String keyName = ZLAndroidKeyUtil.getKeyNameByCode(keyCode);
					if (myKeyUnderTracking.equals(keyName)) {
						final boolean longPress = System.currentTimeMillis() >
							myTrackingStartTime + ViewConfiguration.getLongPressTimeout();
						ZLApplication.Instance().doActionByKey(keyName, longPress);
					}
					myKeyUnderTracking = null;
				}
				return true;
			default:
				return false;
		}
	}

	protected int computeVerticalScrollExtent() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (!view.isScrollbarShown()) {
			return 0;
		}
		if (myScrollingInProgress || (myScrollingShift != 0)) {
			final int from = view.getScrollbarThumbLength(ZLView.PageIndex.current);
			final int to = view.getScrollbarThumbLength(myViewPageToScrollTo);
			final int size = myScrollHorizontally ? getWidth() : getMainAreaHeight();
			final int shift = Math.abs(myScrollingShift);
			return (from * (size - shift) + to * shift) / size;
		} else {
			return view.getScrollbarThumbLength(ZLView.PageIndex.current);
		}
	}

	protected int computeVerticalScrollOffset() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (!view.isScrollbarShown()) {
			return 0;
		}
		if (myScrollingInProgress || (myScrollingShift != 0)) {
			final int from = view.getScrollbarThumbPosition(ZLView.PageIndex.current);
			final int to = view.getScrollbarThumbPosition(myViewPageToScrollTo);
			final int size = myScrollHorizontally ? getWidth() : getMainAreaHeight();
			final int shift = Math.abs(myScrollingShift);
			return (from * (size - shift) + to * shift) / size;
		} else {
			return view.getScrollbarThumbPosition(ZLView.PageIndex.current);
		}
	}

	protected int computeVerticalScrollRange() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (!view.isScrollbarShown()) {
			return 0;
		}
		return view.getScrollbarFullSize();
	}

	private int getMainAreaHeight() {
		final ZLView.FooterArea footer = ZLApplication.Instance().getCurrentView().getFooterArea();
		return footer != null ? getHeight() - footer.getHeight() : getHeight();
	}
}
