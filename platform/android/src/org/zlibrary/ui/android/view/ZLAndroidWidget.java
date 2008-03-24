package org.zlibrary.ui.android.view;

import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.view.*;
import android.util.AttributeSet;

import org.zlibrary.core.view.ZLView;
import org.zlibrary.core.view.ZLViewWidget;

public class ZLAndroidWidget extends View {
	private final ZLAndroidPaintContext myPaintContext = new ZLAndroidPaintContext();
	private ZLAndroidViewWidget myViewWidget;

	public ZLAndroidWidget(Context context, AttributeSet attrs, Map inflateParams, int defStyle) {
		super(context, attrs, inflateParams, defStyle);
	}

	public ZLAndroidWidget(Context context, AttributeSet attrs, Map inflateParams) {
		super(context, attrs, inflateParams);
	}

	public ZLAndroidPaintContext getPaintContext() {
		return myPaintContext;
	}

	void setViewWidget(ZLAndroidViewWidget viewWidget) {
		myViewWidget = viewWidget;
	}

	private long myTime;

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (myViewWidget == null) {
			return;
		}
		ZLView view = myViewWidget.getView();
		if (view == null) {
			return;
		}

		final int w = getWidth();
		final int h = getHeight();

		myPaintContext.beginPaint(canvas);
		long start = System.currentTimeMillis();
		final int rotation = myViewWidget.getRotation();
		myPaintContext.setRotation(rotation);
		switch (rotation) {
			case ZLViewWidget.Angle.DEGREES0:
				myPaintContext.setSize(w, h);
				break;
			case ZLViewWidget.Angle.DEGREES90:
				myPaintContext.setSize(h, w);
				canvas.rotate(270, h / 2, h / 2);
				break;
			case ZLViewWidget.Angle.DEGREES180:
				myPaintContext.setSize(w, h);
				canvas.rotate(180, w / 2, h / 2);
				break;
			case ZLViewWidget.Angle.DEGREES270:
				myPaintContext.setSize(h, w);
				canvas.rotate(90, w / 2, w / 2);
				break;
		}
		view.paint();
		if (myTime == 0) {
			//myTime = org.fbreader.formats.fb2.FB2Reader.LoadingTime;
			myTime = System.currentTimeMillis() - org.zlibrary.ui.android.library.ZLAndroidActivity.StartTime;
		} else {
			myTime = System.currentTimeMillis() - start;
		}
		String sTime = "" + myTime;
		myPaintContext.drawString(240, 140, sTime.toCharArray(), 0, sTime.length());
		myPaintContext.endPaint();
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

		ZLView view = myViewWidget.getView();
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
}
