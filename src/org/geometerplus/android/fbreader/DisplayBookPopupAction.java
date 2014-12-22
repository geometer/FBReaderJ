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

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.*;
import android.widget.PopupWindow;
import android.widget.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.*;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.network.opds.OPDSBookItem;

class DisplayBookPopupAction extends FBAndroidAction {
	DisplayBookPopupAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final ZLTextRegion region = (ZLTextRegion)params[0];
		if (region == null || !(region.getSoul() instanceof BookRegionSoul)) {
			return;
		}
		final BookElement element = ((BookRegionSoul)region.getSoul()).Element;
		if (!element.isInitialized()) {
			return;
		}

		final View bookView = BaseActivity.getLayoutInflater().inflate(R.layout.book_popup, null);
		final int inch = (int)TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_IN, 1, BaseActivity.getResources().getDisplayMetrics()
		);
		final PopupWindow popup = new PopupWindow(bookView, 4 * inch, 3 * inch);
		popup.setFocusable(true);
		popup.setOutsideTouchable(true);
		/*
		popup.setTouchable(true);*/
		popup.setElevation(TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 14, BaseActivity.getResources().getDisplayMetrics()
		));
		popup.setTouchInterceptor(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				popup.dismiss();
				return true;
			}
		});
//		bookView.setSystemUiVisibility(
//			View.SYSTEM_UI_FLAG_LOW_PROFILE |
//			2048 /*View.SYSTEM_UI_FLAG_IMMERSIVE*/ |
//			4096 /*View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY*/ |
//			4 /*View.SYSTEM_UI_FLAG_FULLSCREEN*/ |
//			View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//		);
		popup.showAtLocation(BaseActivity.getCurrentFocus(), Gravity.CENTER, 0, 0);

		final ImageView coverView = (ImageView)bookView.findViewById(R.id.book_popup_cover);
		final ZLAndroidImageData imageData = (ZLAndroidImageData)element.getImageData();
		if (imageData != null) {
			coverView.setImageBitmap(imageData.getFullSizeBitmap());
		}

		final OPDSBookItem item = element.getItem();

		final TextView headerView = (TextView)bookView.findViewById(R.id.book_popup_header_text);
		final StringBuilder text = new StringBuilder();
		for (OPDSBookItem.AuthorData author : item.Authors) {
			text.append("<p><i>").append(author.DisplayName).append("</i></p>");
		}
		text.append("<h3>").append(item.Title).append("</h3>");
		headerView.setText(Html.fromHtml(text.toString()));

		final TextView descriptionView = (TextView)bookView.findViewById(R.id.book_popup_description_text);
		descriptionView.setText(item.getSummary());
		descriptionView.setMovementMethod(new LinkMovementMethod());

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		final View buttonsView = bookView.findViewById(R.id.book_popup_buttons);
		final Button downloadButton = (Button)buttonsView.findViewById(R.id.ok_button);
		downloadButton.setText(buttonResource.getResource("download").getValue());
		final Button cancelButton = (Button)buttonsView.findViewById(R.id.cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				popup.dismiss();
			}
		});
		popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
			public void onDismiss() {
				BaseActivity.hideBars();
			}
		});
		
		System.err.println("Hello, BOOK: " + region.getLeft() + ", " + region.getTop() + ", " + region.getRight() + ", " + region.getBottom());
	}
}
