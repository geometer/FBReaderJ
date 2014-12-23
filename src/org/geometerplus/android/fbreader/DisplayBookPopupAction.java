/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.*;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;

class DisplayBookPopupAction extends FBAndroidAction {
	DisplayBookPopupAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		if (params.length != 1 || !(params[0] instanceof ZLTextRegion)) {
			return;
		}
		final ZLTextRegion region = (ZLTextRegion)params[0];
		if (!(region.getSoul() instanceof BookRegionSoul)) {
			return;
		}
		final BookElement element = ((BookRegionSoul)region.getSoul()).Element;
		if (!element.isInitialized()) {
			return;
		}

		final View mainView = (View)BaseActivity.getViewWidget();
		final View bookView = BaseActivity.getLayoutInflater().inflate(
			ColorProfile.NIGHT.equals(Reader.ViewOptions.ColorProfileName.getValue())
				? R.layout.book_popup_night : R.layout.book_popup,
			null
		);
		final int inch = (int)TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_IN, 1, BaseActivity.getResources().getDisplayMetrics()
		);
		final PopupWindow popup = new PopupWindow(
			bookView,
			Math.min(4 * inch, mainView.getWidth() * 9 / 10),
			Math.min(3 * inch, mainView.getHeight() * 9 / 10)
		);
		popup.setFocusable(true);
		popup.setOutsideTouchable(true);

		final ImageView coverView = (ImageView)bookView.findViewById(R.id.book_popup_cover);
		if (coverView != null) {
			final ZLAndroidImageData imageData = (ZLAndroidImageData)element.getImageData();
			if (imageData != null) {
				coverView.setImageBitmap(imageData.getFullSizeBitmap());
			}
		}

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		final View buttonsView = bookView.findViewById(R.id.book_popup_buttons);

		final Button downloadButton = (Button)buttonsView.findViewById(R.id.ok_button);
		downloadButton.setText(buttonResource.getResource("download").getValue());
		downloadButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// TODO: implement
				popup.dismiss();
			}
		});

		final Button cancelButton = (Button)buttonsView.findViewById(R.id.cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				popup.dismiss();
			}
		});

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			downloadButton.setTextColor(0xFF777777);
			cancelButton.setTextColor(0xFF777777);
		}

		popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
			public void onDismiss() {
			}
		});

		popup.showAtLocation(BaseActivity.getCurrentFocus(), Gravity.CENTER, 0, 0);
	}
}
