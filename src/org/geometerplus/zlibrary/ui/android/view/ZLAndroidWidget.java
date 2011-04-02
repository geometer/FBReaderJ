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
	private Bitmap myMainBitmap;
	private Bitmap mySecondaryBitmap;
	private boolean mySecondaryBitmapIsUpToDate;
	private Bitmap myFooterBitmap;

	private static enum ScrollingState {
		NoScrolling,
		ManualScrolling,
		AutoScrollingForward,
		AutoScrollingBackward
	}
	private ScrollingState myScrollingState = ScrollingState.NoScrolling;
	
	private int myStartX;
	private int myStartY;
	private int myEndX;
	private int myEndY;

	private ZLView.PageIndex myPageToScrollTo = ZLView.PageIndex.current;
	private boolean myScrollHorizontally;

	private float myScrollingSpeed;

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
			myScrollingState = ScrollingState.NoScrolling;
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

		if (myScrollingState == ScrollingState.NoScrolling) {
			onDrawStatic(canvas);
			ZLApplication.Instance().onRepaintFinished();
		} else {
			onDrawInScrolling(canvas);
		}
	}

	private AnimationProvider myAnimationProvider;
	private ZLView.Animation myAnimationType;
	private AnimationProvider getAnimationProvider() {
		final ZLView.Animation type = ZLApplication.Instance().getCurrentView().getAnimationType();
		if (myAnimationProvider == null || myAnimationType != type) {
			myAnimationType = type;
			switch (type) {
				case none:
					myAnimationProvider = new NoneAnimationProvider(myPaint);
					break;
				case curl:
					myAnimationProvider = new CurlAnimationProvider(myPaint);
					break;
				case slide:
					myAnimationProvider = new SlideAnimationProvider(myPaint);
					break;
				case shift:
					myAnimationProvider = new ShiftAnimationProvider(myPaint);
					break;
			}
		}
		return myAnimationProvider;
	}

	private void onDrawInScrolling(Canvas canvas) {
		final ZLView view = ZLApplication.Instance().getCurrentView();

		final int w = getWidth();
		final int h = getMainAreaHeight();

		boolean doStopScrolling = false;
		if (myScrollingState == ScrollingState.AutoScrollingForward ||
			myScrollingState == ScrollingState.AutoScrollingBackward) {
			if (myScrollHorizontally) {
				myEndX += (int)myScrollingSpeed;
			} else {
				myEndY += (int)myScrollingSpeed;
			}
			final int bound;
			if (myScrollingState == ScrollingState.AutoScrollingForward) {
				bound = myScrollHorizontally ? getWidth() : getMainAreaHeight();
			} else {
				bound = 0;
			}
			if (myScrollingSpeed > 0) {
				if (getAnimationProvider().getScrollingShift() >= bound) {
					if (myScrollHorizontally) {
						myEndX = myStartX + bound;
					} else {
						myEndY = myStartY + bound;
					}
					doStopScrolling = true;
				}
			} else {
				if (getAnimationProvider().getScrollingShift() <= -bound) {
					if (myScrollHorizontally) {
						myEndX = myStartX - bound;
					} else {
						myEndY = myStartY - bound;
					}
					doStopScrolling = true;
				}
			}
			myScrollingSpeed *= 1.5;
		}

		if (doStopScrolling) {
			if (myScrollingState != ScrollingState.AutoScrollingBackward) {
				Bitmap swap = myMainBitmap;
				myMainBitmap = mySecondaryBitmap;
				mySecondaryBitmap = swap;
				view.onScrollingFinished(myPageToScrollTo);
				ZLApplication.Instance().onRepaintFinished();
			} else {
				view.onScrollingFinished(ZLView.PageIndex.current);
			}
			setPageToScrollTo(ZLView.PageIndex.current);
			myScrollingState = ScrollingState.NoScrolling;
			onDrawStatic(canvas);
			return;
		}

		getAnimationProvider().setup(
			myStartX, myStartY,
			myEndX, myEndY,
			myScrollHorizontally,
			getWidth(), getMainAreaHeight()
		);
		getAnimationProvider().draw(
			canvas,
			mySecondaryBitmap, myMainBitmap
		);

		if (myScrollingState == ScrollingState.AutoScrollingForward ||
			myScrollingState == ScrollingState.AutoScrollingBackward) {
			postInvalidate();
		}

		drawFooter(canvas);
	}

	private void setPageToScrollTo(ZLView.PageIndex pageIndex) {
		if (myPageToScrollTo != pageIndex) {
			myPageToScrollTo = pageIndex;
			mySecondaryBitmapIsUpToDate = false;
		}
	}

	public void scrollManually(int startX, int startY, int endX, int endY, boolean horizontally) {
		if (myMainBitmap == null) {
			return;
		}

		myScrollingState = ScrollingState.ManualScrolling;
		myScrollHorizontally = horizontally;
		myStartX = startX;
		myStartY = startY;
		myEndX = endX;
		myEndY = endY;

		getAnimationProvider().setup(
			startX, startY,
			endX, endY,
			horizontally,
			getWidth(), getMainAreaHeight()
		);
		setPageToScrollTo(getAnimationProvider().getPageToScrollTo());
		drawOnBitmap(mySecondaryBitmap);
		postInvalidate();
	}

	public void scrollToCenter() {
		myScrollingState = ScrollingState.NoScrolling;
		if (myMainBitmap == null) {
			return;
		}
		setPageToScrollTo(ZLView.PageIndex.current);
		drawOnBitmap(mySecondaryBitmap);
		postInvalidate();
	}

	public void startAutoScrolling(ZLView.PageIndex pageIndex, boolean horizontally) {
		if (myMainBitmap == null) {
			return;
		}
		myScrollHorizontally = horizontally;
		boolean doSetup = myScrollingState == ScrollingState.NoScrolling;
		switch (pageIndex) {
			case current:
				switch (myPageToScrollTo) {
					case current:
						myScrollingState = ScrollingState.NoScrolling;
						myScrollingSpeed = 0;
						break;
					case previous:
						myScrollingState = ScrollingState.AutoScrollingBackward;
						myScrollingSpeed = -3;
						break;
					case next:
						myScrollingState = ScrollingState.AutoScrollingBackward;
						myScrollingSpeed = 3;
						break;
				}
				break;
			case previous:
				myScrollingState = ScrollingState.AutoScrollingForward;
				myScrollingSpeed = 3;
				setPageToScrollTo(pageIndex);
				break;
			case next:
				myScrollingState = ScrollingState.AutoScrollingForward;
				myScrollingSpeed = -3;
				setPageToScrollTo(pageIndex);
				break;
		}
		System.err.println(doSetup + ":" + myScrollingState);
		if (doSetup && myScrollingState != ScrollingState.NoScrolling) {
			//getAnimationProvider().setup(
			//	myScrollHorizontally,
			//	getWidth(), getMainAreaHeight()
			//);
			if (myScrollHorizontally) {
				myStartX = myScrollingSpeed < 0 ? getWidth() : 0;
				myStartY = 0;
			} else {
				myStartX = 0;
				myStartY = myScrollingSpeed < 0 ? getMainAreaHeight() : 0;
			}
			myEndX = myStartX;
			myEndY = myStartY;
			getAnimationProvider().setup(
				myStartX, myStartY,
				myEndX, myEndY,
				myScrollHorizontally,
				getWidth(), getMainAreaHeight()
			);
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
			bitmap == myMainBitmap ? ZLView.PageIndex.current : myPageToScrollTo
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
		if (myScrollingState == ScrollingState.NoScrolling) {
			return view.getScrollbarThumbLength(ZLView.PageIndex.current);
		} else {
			final int from = view.getScrollbarThumbLength(ZLView.PageIndex.current);
			final int to = view.getScrollbarThumbLength(myPageToScrollTo);
			final int size = myScrollHorizontally ? getWidth() : getMainAreaHeight();
			final int shift = Math.abs(getAnimationProvider().getScrollingShift());
			return (from * (size - shift) + to * shift) / size;
		}
	}

	protected int computeVerticalScrollOffset() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (!view.isScrollbarShown()) {
			return 0;
		}
		if (myScrollingState == ScrollingState.NoScrolling) {
			return view.getScrollbarThumbPosition(ZLView.PageIndex.current);
		} else {
			final int from = view.getScrollbarThumbPosition(ZLView.PageIndex.current);
			final int to = view.getScrollbarThumbPosition(myPageToScrollTo);
			final int size = myScrollHorizontally ? getWidth() : getMainAreaHeight();
			final int shift = Math.abs(getAnimationProvider().getScrollingShift());
			return (from * (size - shift) + to * shift) / size;
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
