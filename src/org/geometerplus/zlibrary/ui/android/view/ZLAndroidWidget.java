/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.util.AttributeSet;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.text.view.ZLTextView;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivity;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidKeyUtil;

public class ZLAndroidWidget extends View {
	private final Paint myPaint = new Paint();
	private Bitmap myMainBitmap;
	private Bitmap mySecondaryBitmap;
	private boolean mySecondaryBitmapIsUpToDate;
	private Bitmap myFooterBitmap;

	private boolean myScrollingInProgress;
	private int myScrollingShift;
	private float myScrollingSpeed;
	private int myScrollingBound;

	private final int FOOTER_LONG_TAP_REVERT = 0;
	private final int FOOTER_LONG_TAP_NAVIGATE = 1;

	public ZLTapZones myTapZones = new ZLTapZones();
	public static final int MODE_READ = 0;
	public static final int MODE_TAP_ZONES_EDIT = 1;
	public int myMode = MODE_READ;

	public Activity myMainActivity;
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
		setDrawingCacheEnabled(false);

		// next line prevent ignoring first onKeyDown DPad event
		// after any dialog was closed
		setFocusableInTouchMode(true);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (myScreenIsTouched) {
			final ZLView view = ZLApplication.Instance().getCurrentView();
			myScrollingInProgress = false;
			myScrollingShift = 0;
			myScreenIsTouched = false;
			view.onScrollingFinished(ZLView.PAGE_CENTRAL);
			setPageToScroll(ZLView.PAGE_CENTRAL);
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

		if ((myMainBitmap != null) && ((myMainBitmap.getWidth() != w) || (myMainBitmap.getHeight() != h))) {
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

		if (myScrollingInProgress || (myScrollingShift != 0)) {
			onDrawInScrolling(canvas);
		} else {
			onDrawStatic(canvas);
			ZLApplication.Instance().onRepaintFinished();
		}

		if (myMode == MODE_TAP_ZONES_EDIT) {
			myTapZones.draw(canvas);
		}
	}

	private void onDrawInScrolling(Canvas canvas) {
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
		final boolean horizontal =
			(myViewPageToScroll == ZLView.PAGE_RIGHT) ||
			(myViewPageToScroll == ZLView.PAGE_LEFT);
		canvas.drawBitmap(
			myMainBitmap,
			horizontal ? myScrollingShift : 0,
			horizontal ? 0 : myScrollingShift,
			myPaint
		);
		final int size = horizontal ? w : h;
		int shift = (myScrollingShift < 0) ? (myScrollingShift + size) : (myScrollingShift - size);
		canvas.drawBitmap(
			mySecondaryBitmap,
			horizontal ? shift : 0,
			horizontal ? 0 : shift,
			myPaint
		);

		if (stopScrolling) {
			final ZLView view = ZLApplication.Instance().getCurrentView();
			if (myScrollingBound != 0) {
				Bitmap swap = myMainBitmap;
				myMainBitmap = mySecondaryBitmap;
				mySecondaryBitmap = swap;
				mySecondaryBitmapIsUpToDate = false;
				view.onScrollingFinished(myViewPageToScroll);
				ZLApplication.Instance().onRepaintFinished();
			} else {
				view.onScrollingFinished(ZLView.PAGE_CENTRAL);
			}
			setPageToScroll(ZLView.PAGE_CENTRAL);
			myScrollingInProgress = false;
			myScrollingShift = 0;
		} else {
			if (shift < 0) {
				shift += size;
			}
			// TODO: set color
			myPaint.setColor(Color.rgb(127, 127, 127));
			if (horizontal) {
				canvas.drawLine(shift, 0, shift, h + 1, myPaint);
			} else {
				canvas.drawLine(0, shift, w + 1, shift, myPaint);
			}
			if (myScrollingInProgress) {
				postInvalidate();
			}
		}

		drawFooter(canvas);
	}

	private int myViewPageToScroll = ZLView.PAGE_CENTRAL;
	private void setPageToScroll(int viewPage) {
		if (myViewPageToScroll != viewPage) {
			myViewPageToScroll = viewPage;
			mySecondaryBitmapIsUpToDate = false;
		}
	}

	void scrollToPage(int viewPage, int shift) {
		switch (viewPage) {
			case ZLView.PAGE_BOTTOM:
			case ZLView.PAGE_RIGHT:
				shift = -shift;
				break;
		}

		if (myMainBitmap == null) {
			return;
		}
		if (((shift > 0) && (myScrollingShift <= 0)) ||
			((shift < 0) && (myScrollingShift >= 0))) {
			mySecondaryBitmapIsUpToDate = false;
		}
		myScrollingShift = shift;
		setPageToScroll(viewPage);
		drawOnBitmap(mySecondaryBitmap);
		postInvalidate();
	}

	void startAutoScrolling(int viewPage) {
		if (myMainBitmap == null) {
			return;
		}
		myScrollingInProgress = true;
		switch (viewPage) {
			case ZLView.PAGE_CENTRAL:
				switch (myViewPageToScroll) {
					case ZLView.PAGE_CENTRAL:
						myScrollingSpeed = 0;
						break;
					case ZLView.PAGE_LEFT:
					case ZLView.PAGE_TOP:
						myScrollingSpeed = -3;
						break;
					case ZLView.PAGE_RIGHT:
					case ZLView.PAGE_BOTTOM:
						myScrollingSpeed = 3;
						break;
				}
				myScrollingBound = 0;
				break;
			case ZLView.PAGE_LEFT:
				myScrollingSpeed = 3;
				myScrollingBound = getWidth();
				break;
			case ZLView.PAGE_RIGHT:
				myScrollingSpeed = -3;
				myScrollingBound = -getWidth();
				break;
			case ZLView.PAGE_TOP:
				myScrollingSpeed = 3;
				myScrollingBound = getMainAreaHeight();
				break;
			case ZLView.PAGE_BOTTOM:
				myScrollingSpeed = -3;
				myScrollingBound = -getMainAreaHeight();
				break;
		}
		if (viewPage != ZLView.PAGE_CENTRAL) {
			setPageToScroll(viewPage);
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
		view.paint(context, (bitmap == myMainBitmap) ? ZLView.PAGE_CENTRAL : myViewPageToScroll);
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
		} else if (event.getY() > 0) {
			onKeyDown(KeyEvent.KEYCODE_DPAD_DOWN, null);
		} else if (event.getY() < 0) {
			onKeyDown(KeyEvent.KEYCODE_DPAD_UP, null);
		}
		return true;
	}


	public String myLongPressWord;
	private class LongClickRunnable implements Runnable {
		public void run() {
			final ZLView view = ZLApplication.Instance().getCurrentView();
			if (view instanceof ZLTextView) {
				myLongPressWord = ((ZLTextView)view).getWordUnderPosition(myPressedX, myPressedY);
			}

			if (performLongClick()) {
				myLongClickPerformed = true;
			}
		}
	}

	private LongClickRunnable myPendingLongClickRunnable;
	private boolean myLongClickPerformed;

	private void postLongClickRunnable() {
        myLongClickPerformed = false;
        if (myPendingLongClickRunnable == null) {
            myPendingLongClickRunnable = new LongClickRunnable();
        }
        postDelayed(myPendingLongClickRunnable, ViewConfiguration.getLongPressTimeout());
    }

	private boolean myPendingPress;
	private int myPressedX, myPressedY;
	private boolean myScreenIsTouched;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int)event.getX();
		int y = (int)event.getY();

		final ZLView view = ZLApplication.Instance().getCurrentView();
		switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				if (!myLongClickPerformed) {
					if (myPendingLongClickRunnable != null) {
						removeCallbacks(myPendingLongClickRunnable);
					}
					if (myPendingPress) {
						switch(myMode) {
							case MODE_READ:
								//if (y > getHeight() - myFooter.getTapHeight()) {
								if (y > getHeight() - 40) {
									FBReaderApp reader = (FBReaderApp)FBReaderApp.Instance();
									if (reader.FooterLongTapOption.getValue() == FOOTER_LONG_TAP_REVERT) {
										if (view instanceof ZLTextView) {
											((ZLTextView) view).savePosition();
										}
										//myFooter.setProgress(view, myPressedX);
									}
								} else {
									if (!view.onStylusPress(myPressedX, myPressedY)) {
										myTapZones.doTap(myPressedX, myPressedY);
									}
								}
								break;
							case MODE_TAP_ZONES_EDIT:
								myTapZones.onStylusPress(myPressedX, myPressedY);
								break;
						}
					}
					switch(myMode) {
						case MODE_READ:
							view.onStylusRelease(x, y);
							break;
						case MODE_TAP_ZONES_EDIT:
							myTapZones.onStylusRelease(x, y);
							break;
					}
				}
				myPendingPress = false;
				myScreenIsTouched = false;
				break;
			case MotionEvent.ACTION_DOWN:
				postLongClickRunnable();
				myScreenIsTouched = true;
				myPendingPress = true;
				myPressedX = x;
				myPressedY = y;
				break;
			case MotionEvent.ACTION_MOVE:
				if (!myLongClickPerformed) {
					if (myPendingPress) {
						final int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
						if (Math.abs(myPressedX - x) > slop || Math.abs(myPressedY - y) > slop) {
							if (myPendingLongClickRunnable != null) {
								removeCallbacks(myPendingLongClickRunnable);
							}
							switch(myMode) {
								case MODE_READ:
									view.onStylusMovePressed(myPressedX, myPressedY);
									break;
								case MODE_TAP_ZONES_EDIT:
									myTapZones.onStylusMovePressed(myPressedX, myPressedY);
									break;
							}
							myPendingPress = false;
						}
					}
					if (!myPendingPress) {
						switch(myMode) {
							case MODE_READ:
								view.onStylusMovePressed(x, y);
								break;
							case MODE_TAP_ZONES_EDIT:
								myTapZones.onStylusMovePressed(x, y);
								break;
						}
					}
				}
				break;
		}

		return true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_CAMERA:
				return ZLApplication.Instance().doActionByKey(keyCode);
			default:
				return super.onKeyDown(keyCode, event);
		}
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_CAMERA:
				return true;
			default:
				return super.onKeyUp(keyCode, event);
		}
	}

	protected int computeVerticalScrollExtent() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (!view.isScrollbarShown()) {
			return 0;
		}
		if (myScrollingInProgress || (myScrollingShift != 0)) {
			final int from = view.getScrollbarThumbLength(ZLView.PAGE_CENTRAL);
			final int to = view.getScrollbarThumbLength(myViewPageToScroll);
			final boolean horizontal =
				(myViewPageToScroll == ZLView.PAGE_RIGHT) ||
				(myViewPageToScroll == ZLView.PAGE_LEFT);
			final int size = horizontal ? getWidth() : getMainAreaHeight();
			final int shift = Math.abs(myScrollingShift);
			return (from * (size - shift) + to * shift) / size;
		} else {
			return view.getScrollbarThumbLength(ZLView.PAGE_CENTRAL);
		}
	}

	protected int computeVerticalScrollOffset() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (!view.isScrollbarShown()) {
			return 0;
		}
		if (myScrollingInProgress || (myScrollingShift != 0)) {
			final int from = view.getScrollbarThumbPosition(ZLView.PAGE_CENTRAL);
			final int to = view.getScrollbarThumbPosition(myViewPageToScroll);
			final boolean horizontal =
				(myViewPageToScroll == ZLView.PAGE_RIGHT) ||
				(myViewPageToScroll == ZLView.PAGE_LEFT);
			final int size = horizontal ? getWidth() : getMainAreaHeight();
			final int shift = Math.abs(myScrollingShift);
			return (from * (size - shift) + to * shift) / size;
		} else {
			return view.getScrollbarThumbPosition(ZLView.PAGE_CENTRAL);
		}
	}

	protected int computeVerticalScrollRange() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (!view.isScrollbarShown()) {
			return 0;
		}
		return view.getScrollbarFullSize();
	}

	ZLResource myMenuResource;
	public void loadContextMenu(Menu menu, ArrayList<String> menuActions) {
		String read_menu[] = {
				ActionCode.SEARCH,
				ActionCode.INITIATE_COPY,
				ActionCode.TRANSLATE,
				ActionCode.ROTATE,
				ActionCode.INCREASE_FONT,
				ActionCode.DECREASE_FONT,
				ActionCode.SHOW_NAVIGATION,
				ActionCode.SHOW_BOOK_INFO,
				ActionCode.TAP_ZONES,
		};

		String zones_menu[] = {
				ActionCode.TAP_ZONE_SELECT_ACTION,
				ActionCode.TAP_ZONE_ADD,
				ActionCode.TAP_ZONE_DELETE,
				ActionCode.TAP_ZONES_SAVE,
				ActionCode.TAP_ZONES_CANCEL,
		};

		String actions[];
		if (myMode == MODE_READ) {
			actions = read_menu;
		} else if (myMode == MODE_TAP_ZONES_EDIT) {
			actions = zones_menu;
		} else {
			return;
		}

		if (myMenuResource == null) {
			myMenuResource = ZLResource.resource("actions");
		}

		menuActions.clear();
		ZLApplication app = ZLApplication.Instance();
		for (int actionIndex = 0; actionIndex < actions.length; ++actionIndex) {
			String actionId = actions[actionIndex];
			if (app.isActionEnabled(actionId) && app.isActionVisible(actionId)) {
				String itemText = myMenuResource.getResource(actionId).getValue();
				if (actionId == ActionCode.TRANSLATE) {
					itemText = String.format(itemText, myLongPressWord);
				}
				menu.add(0, actionIndex, Menu.NONE, itemText);
			}
			menuActions.add(actionId);
		}
	}

	public boolean onLongClick (){
		//if (myPressedY > getHeight() - myFooter.getTapHeight()) {
		if (myPressedY > getHeight() - 40) {
			FBReaderApp reader = (FBReaderApp)FBReaderApp.Instance();
			if (reader.FooterLongTapOption.getValue() == FOOTER_LONG_TAP_REVERT) {
				final ZLView view = ZLApplication.Instance().getCurrentView();
				if (view instanceof ZLTextView) {
					return ((ZLTextView) view).revertPosition();
				}
			}
			if (reader.FooterLongTapOption.getValue() == FOOTER_LONG_TAP_NAVIGATE) {
				final ZLView view = ZLApplication.Instance().getCurrentView();
				//myFooter.setProgress(view, myPressedX);
				return true;
			}
		}
		return false;
	}

	public Point getTapPoint() {
		return new Point(myPressedX, myPressedY);
	}

	private int getMainAreaHeight() {
		final ZLView.FooterArea footer = ZLApplication.Instance().getCurrentView().getFooterArea();
		return footer != null ? getHeight() - footer.getHeight() : getHeight();
	}
}
