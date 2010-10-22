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

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.view.ZLTextView;

// TODO: remove these dependencies
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;

class ZLFooter {
	private float myGaugeEnd;

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
		float progress = 1.0f * Math.min(x, myGaugeEnd) / myGaugeEnd;
		int page = (int)(progress * textView.computePageNumber());
		if (page <= 1) {
			textView.gotoHome();
		} else {
			textView.gotoPage(page);
		}
		ZLApplication.Instance().repaintView();
	}

	void paint(ZLPaintContext context) {
		final ZLTextView view = (ZLTextView)ZLApplication.Instance().getCurrentView();

		final int width = context.getWidth();
		final int height = view.getFooterArea().getHeight();
		final int lineWidth = height <= 10 ? 1 : 2;
		final int delta = height <= 10 ? 0 : 1;
		context.setFont(
			"sans-serif",
			height <= 10 ? height + 3 : height + 1,
			height > 10, false, false
		);

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

		final int infoWidth = context.getStringWidth(infoString);

		myGaugeEnd = width - ((infoWidth == 0) ? 0 : infoWidth + 10);
	}
}
