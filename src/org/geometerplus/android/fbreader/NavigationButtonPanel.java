/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

final class NavigationButtonPanel extends ControlButtonPanel {
	private volatile boolean myIsInProgress;

	NavigationButtonPanel(FBReaderApp fbReader) {
		super(fbReader);
	}

	public void runNavigation() {
		if (!getVisibility()) {
			myIsInProgress = false;
			initPosition();
			show(true);
		}
	}

	@Override
	public void onShow() {
		if (myControlPanel != null) {
			setupNavigation(myControlPanel);
		}
	}

	@Override
	public void updateStates() {
		super.updateStates();
		if (!myIsInProgress && myControlPanel != null) {
			setupNavigation(myControlPanel);
		}
	}

	@Override
	public void createControlPanel(FBReader activity, RelativeLayout root) {
		myControlPanel = new ControlPanel(activity, root, true);

		final View layout = activity.getLayoutInflater().inflate(R.layout.navigate, myControlPanel, false);

		final SeekBar slider = (SeekBar)layout.findViewById(R.id.book_position_slider);
		final TextView text = (TextView)layout.findViewById(R.id.book_position_text);

		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			private void gotoPage(int page) {
				final ZLTextView view = Reader.getTextView();
				if (page == 1) {
					view.gotoHome();
				} else {
					view.gotoPage(page);
				}
				Reader.repaintView();
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				myIsInProgress = false;
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				myIsInProgress = true;
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					final int page = progress + 1;
					final int pagesNumber = seekBar.getMax() + 1;
					text.setText(makeProgressText(page, pagesNumber));
					gotoPage(page);
				}
			}
		});

		final Button btnOk = (Button)layout.findViewById(android.R.id.button1);
		final Button btnCancel = (Button)layout.findViewById(android.R.id.button3);
		View.OnClickListener listener = new View.OnClickListener() {
			public void onClick(View v) {
				final ZLTextWordCursor position = StartPosition;
				if (v == btnCancel && position != null) {
					Reader.getTextView().gotoPosition(position);
				} else if (v == btnOk) {
					storePosition();
				}
				StartPosition = null;
				hide(true);
			}
		};
		btnOk.setOnClickListener(listener);
		btnCancel.setOnClickListener(listener);
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		btnOk.setText(buttonResource.getResource("ok").getValue());
		btnCancel.setText(buttonResource.getResource("cancel").getValue());

		myControlPanel.addView(layout);
	}

	private void setupNavigation(ControlPanel panel) {
		final SeekBar slider = (SeekBar)panel.findViewById(R.id.book_position_slider);
		final TextView text = (TextView)panel.findViewById(R.id.book_position_text);

		final ZLTextView textView = Reader.getTextView();
		final int page = textView.computeCurrentPage();
		final int pagesNumber = textView.computePageNumber();

		if (slider.getMax() != pagesNumber - 1 || slider.getProgress() != page - 1) {
			slider.setMax(pagesNumber - 1);
			slider.setProgress(page - 1);
			text.setText(makeProgressText(page, pagesNumber));
		}
	}

	private static String makeProgressText(int page, int pagesNumber) {
		return page + " / " + pagesNumber;
	}
}
