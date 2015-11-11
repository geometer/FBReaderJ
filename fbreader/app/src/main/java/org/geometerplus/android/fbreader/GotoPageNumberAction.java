/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader;

import org.geometerplus.zlibrary.text.view.ZLTextView;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

import org.geometerplus.android.util.GotoPageDialogUtil;

public class GotoPageNumberAction extends FBAndroidAction {
	GotoPageNumberAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final ZLTextView textView = Reader.getTextView();
		final ZLTextView.PagePosition pagePosition = textView.pagePosition();

		GotoPageDialogUtil.showDialog(
			BaseActivity,
			new GotoPageDialogUtil.PageSelector() {
				@Override
				public void gotoPage(int page) {
					textView.gotoPage(page);
					Reader.getViewWidget().reset();
					Reader.getViewWidget().repaint();
					BaseActivity.hideBars();
				}
			},
			pagePosition.Current,
			pagePosition.Total
		);
	}
}
