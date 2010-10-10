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

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.fbreader.FBReader;

public class ZLFooter {
	private final int SIZE_SMALL = 0;
	private final int SIZE_NORMAL = 1;
	private final int SIZE_LARGE = 2;

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

	public int getHeight() {
		return mySize.y;
	}

	public int getTapHeight() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (view.scrollbarType() == ZLView.SCROLLBAR_SHOW_AS_FOOTER) {
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
		canvas.drawBitmap(myBitmap, 0, myDrawAreaSize.y - getHeight(), myBgPaint);
	}

	public void setProgress(ZLView view, int x) {
		// set progress according to tap coordinate
		view.setProgress(((float)Math.max(myGaugeStart, Math.min(x, myGaugeEnd)) - myGaugeStart) / (myGaugeEnd -myGaugeStart));
	}

	private void updateBitmap(float scrollProgress) {
		// if it is first drawing of bitmap or footer size is changed
		boolean gaugeChanged = false;
		boolean infoChanged = false;

		// query colors for background and regular text
		FBReader reader = (FBReader)FBReader.Instance();
		int bgColor = ZLAndroidColorUtil.rgb(reader.BookTextView.getBackgroundColor());
		int fgColor = ZLAndroidColorUtil.rgb(reader.BookTextView.getTextColor(FBHyperlinkType.NONE));
		if (myLastFgColor != fgColor || myLastBgColor != bgColor) {
			gaugeChanged = true;
			infoChanged = true;
		}

		int size = reader.FooterSizeOption.getValue();
		int delta = (size == SIZE_SMALL ? 0 : 1);
		if (size != myLastSize) {
			int fontStyle = Typeface.NORMAL;
			switch (size) {
			case SIZE_SMALL:
				mySize.y = 9;
				myTextPaint.setTextSize(12);
				myFgPaint.setStrokeWidth(1);
				break;
			case SIZE_NORMAL:
				mySize.y = 13;
				myTextPaint.setTextSize(14);
				myFgPaint.setStrokeWidth(2);
				fontStyle = Typeface.BOLD;
				break;
			case SIZE_LARGE:
				mySize.y = 15;
				myTextPaint.setTextSize(16);
				myFgPaint.setStrokeWidth(2);
				fontStyle = Typeface.BOLD;
				break;
			}
			myTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, fontStyle));
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

		final ZLView view = ZLApplication.Instance().getCurrentView();
		float progress = view.getProgress(scrollProgress);
		if (progress != myLastProgress) {
			gaugeChanged = true;
		}

		final ZLTextView textView = (ZLTextView)view;
		final int pagesProgress = textView.computeCurrentPage();
		final int bookLength = textView.computePageNumber();

		ZLAndroidApplication app = ZLAndroidApplication.Instance();
		Date date = new Date();
		if (infoChanged ||
			myLastShowClock != app.FooterShowClock.getValue() ||
			myLastShowBattery != app.FooterShowBattery.getValue() ||
			myLastShowProgress != app.FooterShowProgress.getValue() ||
			(app.FooterShowClock.getValue() && (myLastHours != date.getHours() || myLastMinutes != date.getMinutes())) ||
			(app.FooterShowProgress.getValue() && (myLastPagesNum != bookLength || myLastPage != pagesProgress)) ||
			(app.FooterShowBattery.getValue() && (myLastBattery != ZLApplication.Instance().myBatteryLevel))){
			infoChanged = true;
		}

		if (infoChanged) {
			String info = "";
			if (app.FooterShowClock.getValue()) {
				info += String.format("%02d:%02d", date.getHours(), date.getMinutes());
			}
			if (app.FooterShowBattery.getValue()) {
				info = String.format("%d%%", ZLApplication.Instance().myBatteryLevel) +
					(info.equals("") ? "" : " ") + info;
			}
			if (app.FooterShowProgress.getValue()) {
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
		myLastShowClock = app.FooterShowClock.getValue();
		myLastShowBattery = app.FooterShowBattery.getValue();
		myLastShowProgress = app.FooterShowProgress.getValue();
		myLastPagesNum = bookLength;
		myLastPage = pagesProgress;
		myLastSize = size;
	}
}
