/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

import android.view.View;
import android.widget.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

final class NavigationPopup {
	private PopupWindow myWindow;
	private ZLTextWordCursor myStartPosition;
	private final FBReaderApp myFBReader;
	private Button myResetButton;

	NavigationPopup(FBReaderApp fbReader) {
		myFBReader = fbReader;
	}

	public void runNavigation(FBReader activity, RelativeLayout root) {
		createControlPanel(activity, root);
		myStartPosition = new ZLTextWordCursor(myFBReader.getTextView().getStartCursor());
		myWindow.show();
		setupNavigation();
	}

	public void update() {
		if (myWindow != null) {
			setupNavigation();
		}
	}

	public void stopNavigation() {
		if (myWindow == null) {
			return;
		}

		if (myStartPosition != null &&
			!myStartPosition.equals(myFBReader.getTextView().getStartCursor())) {
			myFBReader.addInvisibleBookmark(myStartPosition);
			myFBReader.storePosition();
		}
		myWindow.hide();
		myWindow = null;
	}

	public void createControlPanel(FBReader activity, RelativeLayout root) {
		if (myWindow != null && activity == myWindow.getActivity()) {
			return;
		}

		myWindow = new PopupWindow(activity, root, PopupWindow.Location.BottomFlat);

		final View layout = activity.getLayoutInflater().inflate(R.layout.navigate, myWindow, false);

		final SeekBar slider = (SeekBar)layout.findViewById(R.id.navigation_slider);
		final TextView text = (TextView)layout.findViewById(R.id.navigation_text);

		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			private void gotoPage(int page) {
				final ZLTextView view = myFBReader.getTextView();
				if (page == 1) {
					view.gotoHome();
				} else {
					view.gotoPage(page);
				}
				myFBReader.getViewWidget().reset();
				myFBReader.getViewWidget().repaint();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					final int page = progress + 1;
					final int pagesNumber = seekBar.getMax() + 1;
					gotoPage(page);
					text.setText(makeProgressText(page, pagesNumber));
				}
			}
		});

		myResetButton = (Button)layout.findViewById(R.id.navigation_reset_button);
		myResetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (myStartPosition != null) {
					myFBReader.getTextView().gotoPosition(myStartPosition);
				}
				myFBReader.getViewWidget().reset();
				myFBReader.getViewWidget().repaint();
				update();
			}
		});
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		myResetButton.setText(buttonResource.getResource("resetPosition").getValue());

		myWindow.addView(layout);
	}

	private void setupNavigation() {
		final SeekBar slider = (SeekBar)myWindow.findViewById(R.id.navigation_slider);
		final TextView text = (TextView)myWindow.findViewById(R.id.navigation_text);

		final ZLTextView textView = myFBReader.getTextView();
		final ZLTextView.PagePosition pagePosition = textView.pagePosition();

		if (slider.getMax() != pagePosition.Total - 1 || slider.getProgress() != pagePosition.Current - 1) {
			slider.setMax(pagePosition.Total - 1);
			slider.setProgress(pagePosition.Current - 1);
			text.setText(makeProgressText(pagePosition.Current, pagePosition.Total));
		}

		myResetButton.setEnabled(
			myStartPosition != null &&
			!myStartPosition.equals(myFBReader.getTextView().getStartCursor())
		);
	}

	private String makeProgressText(int page, int pagesNumber) {
		final StringBuilder builder = new StringBuilder();
		builder.append(page);
		builder.append("/");
		builder.append(pagesNumber);
		final TOCTree tocElement = myFBReader.getCurrentTOCElement();
		if (tocElement != null) {
			builder.append("  ");
			builder.append(tocElement.getText());
		}
		return builder.toString();
	}
}
