package org.zlibrary.ui.android.view;

import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.util.AttributeSet;

import org.zlibrary.core.view.ZLView;

public class ZLAndroidWidget extends View {
	private final ZLAndroidPaintContext myPaintContext = new ZLAndroidPaintContext();

	public ZLAndroidWidget(Context context, AttributeSet attrs, Map inflateParams, int defStyle) {
		super(context, attrs, inflateParams, defStyle);
	}

	public ZLAndroidWidget(Context context, AttributeSet attrs, Map inflateParams) {
		super(context, attrs, inflateParams);
	}

	public ZLAndroidPaintContext getPaintContext() {
		return myPaintContext;
	}

	public void onSizeChanged(int w, int h, int oldW, int oldH) {
		myPaintContext.setSize(w, h);
	}

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		myPaintContext.beginPaint(canvas);
		ZLView view = ZLAndroidViewWidget.Instance.getView();
		if (view != null) {
			view.paint();
		} else {
			String s = "Hello, Android!";
			int w = myPaintContext.getStringWidth(s);
			int h = myPaintContext.getStringHeight();
			myPaintContext.drawString((myPaintContext.getWidth() - w) / 2, h, s);
		}
		myPaintContext.endPaint();
	}
}
