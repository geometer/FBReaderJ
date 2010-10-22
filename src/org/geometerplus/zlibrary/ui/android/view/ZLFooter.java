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

import android.graphics.Rect;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.text.view.ZLTextView;

import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

// TODO: remove these dependencies
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;

public class ZLFooter {
	private float myGaugeStart;
	private float myGaugeEnd;
	private Rect myGaugeRect;

	private String myInfoString;
	private ZLColor myLastBgColor;
	private ZLColor myLastFgColor;
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

	void paint(ZLPaintContext context) {
		// if it is first drawing of bitmap or footer height is changed
		boolean infoChanged = false;

		// query colors for background and regular text
		final ZLTextView view = (ZLTextView)ZLApplication.Instance().getCurrentView();
		final ZLColor bgColor = view.getBackgroundColor();
		// TODO: separate color option for footer color
		final ZLColor fgColor = view.getTextColor(FBHyperlinkType.NONE);
		if (!fgColor.equals(myLastFgColor) || !bgColor.equals(myLastBgColor)) {
			infoChanged = true;
			myLastFgColor = fgColor;
			myLastBgColor = bgColor;
		}

		final int width = context.getWidth();
		final int height = view.getFooterArea().getHeight();
		final int lineWidth = height <= 10 ? 1 : 2;
		final int delta = height <= 10 ? 0 : 1;
		context.setFont(
			"sans-serif",
			height <= 10 ? height + 3 : height + 1,
			height > 10, false, false
		);

		if (height != myLastHeight) {
			myLastHeight = height;
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
			final int infoWidth = context.getStringWidth(infoString);

			context.clear(bgColor);

			// draw info text
			context.setTextColor(fgColor);
			context.drawString(width - infoWidth, height - delta, infoString);

			myGaugeRect.set(0, 0, width - ((infoWidth == 0) ? 0 : infoWidth + 10), height);

			myGaugeRect.right -= (1 - delta);
			myGaugeRect.inset(1 + delta, 1 + delta);
			myGaugeStart = myGaugeRect.left;
			myGaugeEnd = myGaugeRect.right;

			// compute gauge size
			myGaugeRect.inset(2 + delta, 2 + delta);
			myGaugeRect.right = myGaugeRect.left + (int)((float)myGaugeRect.width() * pagesProgress / bookLength);

			// draw info text back ground rectangle
			context.setLineColor(fgColor);
			context.setLineWidth(lineWidth);
			final int gaugeRight = width - ((infoWidth == 0) ? 0 : infoWidth + 10) - 2;
			context.drawLine(lineWidth, lineWidth, lineWidth, height - lineWidth);
			context.drawLine(lineWidth, height - lineWidth, gaugeRight, height - lineWidth);
			context.drawLine(gaugeRight, height - lineWidth, gaugeRight, lineWidth);
			context.drawLine(gaugeRight, lineWidth, lineWidth, lineWidth);

			final int gaugeInternalRight = 1 + 2 * lineWidth + (int)(1.0 * (gaugeRight - 2 - 3 * lineWidth) * pagesProgress / bookLength);
			context.drawLine(1 + 2 * lineWidth, 1 + 2 * lineWidth, 1 + 2 * lineWidth, height - 1 - 2 * lineWidth);
			context.drawLine(1 + 2 * lineWidth, height - 1 - 2 * lineWidth, gaugeInternalRight, height - 1 - 2 * lineWidth);
			context.drawLine(gaugeInternalRight, height - 1 - 2 * lineWidth, gaugeInternalRight, 1 + 2 * lineWidth);
			context.drawLine(gaugeInternalRight, 1 + 2 * lineWidth, 1 + 2 * lineWidth, 1 + 2 * lineWidth);
		}
	}
}
