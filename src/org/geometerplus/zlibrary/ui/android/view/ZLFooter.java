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
import android.graphics.Rect;
import android.graphics.Typeface;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.view.ZLTextView;

import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

// TODO: remove these dependencies
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;

public class ZLFooter {
	private final Paint myTextPaint = new Paint();
	private final Paint myBgPaint = new Paint();
	private final Paint myFgPaint = new Paint();
	private float myGaugeStart;
	private float myGaugeEnd;
	private Rect myGaugeRect;

	private String myInfoString;
	private int myLastBgColor;
	private int myLastFgColor;
	private int myLastHeight;
	private int myLastWidth;

	public ZLFooter() {
		myGaugeRect = new Rect();
		myLastHeight = -1;
		myLastWidth = -1;
	}

	public int getTapHeight() {
		final ZLView view = ZLApplication.Instance().getCurrentView();
		if (view.getFooterArea() != null) {
			return 30;
		}
		return 0;
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

	void paint(ZLPaintContext context, Canvas canvas) {
		// if it is first drawing of bitmap or footer height is changed
		boolean infoChanged = false;

		// query colors for background and regular text
		final ZLTextView view = (ZLTextView)ZLApplication.Instance().getCurrentView();
		int bgColor = ZLAndroidColorUtil.rgb(view.getBackgroundColor());
		// TODO: separate color option for footer color
		int fgColor = ZLAndroidColorUtil.rgb(view.getTextColor(FBHyperlinkType.NONE));
		if (myLastFgColor != fgColor || myLastBgColor != bgColor) {
			infoChanged = true;
			myLastFgColor = fgColor;
			myLastBgColor = bgColor;
		}

		final int width = context.getWidth();
		final int height = view.getFooterArea().getHeight();

		int delta = height <= 10 ? 0 : 1;
		if (height != myLastHeight) {
			myLastHeight = height;
			myTextPaint.setTextSize(height <= 10 ? height + 3 : height + 1);
			myFgPaint.setStrokeWidth(height <= 10 ? 1 : 2);
			myTextPaint.setTypeface(Typeface.create(
				Typeface.SANS_SERIF, height <= 10 ? Typeface.NORMAL : Typeface.BOLD
			));
			myTextPaint.setTextAlign(Paint.Align.RIGHT);
			myTextPaint.setStyle(Paint.Style.FILL);
			myTextPaint.setAntiAlias(true);
			infoChanged = true;
		}
		if (width != myLastWidth) {
			myLastWidth = width;
			infoChanged = true;
		}

		final int pagesProgress = view.computeCurrentPage();
		final int bookLength = view.computePageNumber();

		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();

		final StringBuilder info = new StringBuilder();
		if (fbReader.FooterShowProgress.getValue()) {
			info.append(pagesProgress);
			info.append("/");
			info.append(bookLength);
		}
		if (fbReader.FooterShowBattery.getValue()) {
			if (info.length() > 0) {
				info.append(" ");
			}
			info.append(ZLApplication.Instance().getBatteryLevel());
			info.append("%");
		}
		if (fbReader.FooterShowClock.getValue()) {
			if (info.length() > 0) {
				info.append(" ");
			}
			Date date = new Date();
			info.append(String.format("%02d:%02d", date.getHours(), date.getMinutes()));
		}
		final String infoString = info.toString();
		if (!infoString.equals(myInfoString)) {
			myInfoString = infoString;
			infoChanged = true;
		}

		if (infoChanged) {
			// calculate information text width and height of gauge
			Rect infoRect = new Rect();
			myTextPaint.getTextBounds(infoString, 0, infoString.length(), infoRect);
			int infoWidth = infoString.equals("") ? 0 : infoRect.width() + 10;

			// draw info text back ground rectangle
			myBgPaint.setColor(bgColor);
			canvas.drawRect(width - infoWidth, 0, width, height, myBgPaint);

			// draw info text
			myTextPaint.setColor(fgColor);
			canvas.drawText(infoString, width - 1, height - delta, myTextPaint);

			// draw info text back ground rectangle
			myBgPaint.setColor(bgColor);
			myGaugeRect.set(0, 0, width - infoWidth, height);
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
			myGaugeRect.right = myGaugeRect.left + (int)((float)myGaugeRect.width() * pagesProgress / bookLength);

			// draw gauge progress
			canvas.drawRect(myGaugeRect, myFgPaint);
		}
	}
}
