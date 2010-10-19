/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.view.ZLTextView;

import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

// TODO: remove this dependency
import org.geometerplus.fbreader.fbreader.FBReader;

public class ZLFooter {
	private Point mySize;
	private Point myDrawAreaSize;
	private Paint myTextPaint;
	private Paint myBgPaint;
	private Paint myFgPaint;
	private float myGaugeStart;
	private float myGaugeEnd;
	private Bitmap myBitmap;
	private Rect myGaugeRect;

	private float myLastProgress;
	private int myLastHours;
	private int myLastPagesNum;
	private int myLastPage;
	private int myLastBattery;
	private int myLastMinutes;
	private int myLastBgColor;
	private int myLastFgColor;
	private int myInfoWidth;
	private int myLastSize;
	private boolean myLastShowClock;
	private boolean myLastShowBattery;
	private boolean myLastShowProgress;


	public ZLFooter() {
		mySize = new Point(0, 9);
		myDrawAreaSize = new Point(0, 0);
		myBgPaint = new Paint();
		myFgPaint = new Paint();
		myGaugeRect = new Rect();
		myInfoWidth = 0;

		myTextPaint = new Paint();
		myLastSize = -1;
	}

	public int getTapHeight() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (view.getFooterArea() != null) {
			return 30;
		}
		return 0;
	}

	public void setDrawAreaSize(int width, int height) {
		mySize.x = width;
		myDrawAreaSize.x = width;
		myDrawAreaSize.y = height;
	}

	public void onDraw(Canvas canvas, float scrollProgress) {
		updateBitmap(scrollProgress);
		// myDrawAreaSize.y is from View.getHeight, do not use canvas.getHeight it is buggy
		canvas.drawBitmap(myBitmap, 0, myDrawAreaSize.y - mySize.y, myBgPaint);
	}

	public void setProgress(ZLView view, int x) {
		// set progress according to tap coordinate
		ZLTextView textView = (ZLTextView)view;
		float progress = 1.0f *
			(Math.max(myGaugeStart, Math.min(x, myGaugeEnd)) - myGaugeStart) /
			(myGaugeEnd - myGaugeStart);
		int page = (int)(progress * textView.computePageNumber());
		if (page <= 1) {
			textView.gotoHome();
		} else {
			textView.gotoPage(page);
		}
		ZLApplication.Instance().repaintView();
	}

	private void updateBitmap(float scrollProgress) {
		// if it is first drawing of bitmap or footer size is changed
		boolean gaugeChanged = false;
		boolean infoChanged = false;

		// query colors for background and regular text
		final ZLTextView view = (ZLTextView)ZLApplication.Instance().getCurrentView();
		int bgColor = ZLAndroidColorUtil.rgb(view.getBackgroundColor());
		int fgColor = ZLAndroidColorUtil.rgb(view.getFooterColor());
		if (myLastFgColor != fgColor || myLastBgColor != bgColor) {
			gaugeChanged = true;
			infoChanged = true;
		}

		int size = view.getFooterArea().getHeight();
		int delta = size <= 10 ? 0 : 1;
		if (size != myLastSize) {
			mySize.y = size;
			myTextPaint.setTextSize(size <= 10 ? size + 3 : size + 1);
			myFgPaint.setStrokeWidth(size <= 10 ? 1 : 2);
			myTextPaint.setTypeface(Typeface.create(
				Typeface.SANS_SERIF, size <= 10 ? Typeface.NORMAL : Typeface.BOLD
			));
			myTextPaint.setTextAlign(Paint.Align.RIGHT);
			myTextPaint.setStyle(Paint.Style.FILL);
			myTextPaint.setAntiAlias(true);
		}

		if (myBitmap == null ||
			myBitmap.getWidth() != mySize.x || myBitmap.getHeight() != mySize.y) {
			myBitmap = Bitmap.createBitmap(mySize.x, mySize.y, Bitmap.Config.RGB_565);
			gaugeChanged = true;
			infoChanged = true;
		}
		Canvas canvas = new Canvas(myBitmap);

		final ZLTextView textView = (ZLTextView)view;
		final int pagesProgress = textView.computeCurrentPage();
		final int bookLength = textView.computePageNumber();

		float progress = 1.0f * pagesProgress / bookLength;
		if (progress != myLastProgress) {
			gaugeChanged = true;
		}

		FBReader fbReader = (FBReader)FBReader.Instance();
		Date date = new Date();
		if (infoChanged ||
			myLastShowClock != fbReader.FooterShowClock.getValue() ||
			myLastShowBattery != fbReader.FooterShowBattery.getValue() ||
			myLastShowProgress != fbReader.FooterShowProgress.getValue() ||
			(fbReader.FooterShowClock.getValue() && (myLastHours != date.getHours() || myLastMinutes != date.getMinutes())) ||
			(fbReader.FooterShowProgress.getValue() && (myLastPagesNum != bookLength || myLastPage != pagesProgress)) ||
			(fbReader.FooterShowBattery.getValue() && (myLastBattery != ZLApplication.Instance().myBatteryLevel))){
			infoChanged = true;
		}

		if (infoChanged) {
			String info = "";
			if (fbReader.FooterShowClock.getValue()) {
				info += String.format("%02d:%02d", date.getHours(), date.getMinutes());
			}
			if (fbReader.FooterShowBattery.getValue()) {
				info = String.format("%d%%", ZLApplication.Instance().myBatteryLevel) +
					(info.equals("") ? "" : " ") + info;
			}
			if (fbReader.FooterShowProgress.getValue()) {
				info = String.format("%d/%d", pagesProgress, bookLength) +
					(info.equals("") ? "" : " ") + info;
			}

			// calculate information text width and size of gauge
			Rect infoRect = new Rect();
			myTextPaint.getTextBounds(info, 0, info.length(), infoRect);
			int infoWidth = (info.equals("") ? 0 : infoRect.width() + 10);
			if (myInfoWidth != infoWidth) {
				gaugeChanged = true;
				myInfoWidth = infoWidth;
			}

			// draw info text back ground rectangle
			myBgPaint.setColor(bgColor);
			canvas.drawRect(mySize.x - myInfoWidth, 0, mySize.x, mySize.y, myBgPaint);

			// draw info text
			myTextPaint.setColor(fgColor);
			canvas.drawText(info, mySize.x - 1, mySize.y - delta, myTextPaint);
		}

		if (gaugeChanged) {
			// draw info text back ground rectangle
			myBgPaint.setColor(bgColor);
			myGaugeRect.set(0, 0, mySize.x - myInfoWidth, mySize.y);
			canvas.drawRect(myGaugeRect, myBgPaint);

			// draw gauge border line
			myFgPaint.setColor(fgColor);
			myFgPaint.setStyle(Paint.Style.STROKE);
			myGaugeRect.right -= (1 - delta);
			myGaugeRect.inset(1 + delta, 1 + delta);
			canvas.drawRect(myGaugeRect, myFgPaint);
			myGaugeStart = myGaugeRect.left;
			myGaugeEnd = myGaugeRect.right;

			// compute gauge size
			myGaugeRect.inset(2 + delta, 2 + delta);
			myGaugeRect.right = myGaugeRect.left + (int)((float)myGaugeRect.width() * progress);

			// draw gauge progress
			canvas.drawRect(myGaugeRect, myFgPaint);
		}

		myLastProgress = progress;
		myLastBattery = ZLApplication.Instance().myBatteryLevel;
		myLastHours = date.getHours();
		myLastMinutes = date.getMinutes();
		myLastFgColor = fgColor;
		myLastBgColor = bgColor;
		myLastShowClock = fbReader.FooterShowClock.getValue();
		myLastShowBattery = fbReader.FooterShowBattery.getValue();
		myLastShowProgress = fbReader.FooterShowProgress.getValue();
		myLastPagesNum = bookLength;
		myLastPage = pagesProgress;
		myLastSize = size;
	}
}
