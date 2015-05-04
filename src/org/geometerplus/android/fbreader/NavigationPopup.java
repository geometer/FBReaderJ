/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

final class NavigationPopup extends ZLApplication.PopupPanel {
	final static String ID = "NavigationPopup";

	private volatile NavigationWindow myWindow;
	private volatile FBReader myActivity;
	private volatile RelativeLayout myRoot;
	private ZLTextWordCursor myStartPosition;
	private final FBReaderApp myFBReader;
	private volatile boolean myIsInProgress;

	NavigationPopup(FBReaderApp fbReader) {
		super(fbReader);
		myFBReader = fbReader;
	}

	public void setPanelInfo(FBReader activity, RelativeLayout root) {
		myActivity = activity;
		myRoot = root;
	}

	public void runNavigation() {
		if (myWindow == null || myWindow.getVisibility() == View.GONE) {
			myIsInProgress = false;
			if (myStartPosition == null) {
				myStartPosition = new ZLTextWordCursor(myFBReader.getTextView().getStartCursor());
			}
			Application.showPopup(ID);
		}
	}

	@Override
	protected void show_() {
		if (myActivity != null) {
			createPanel(myActivity, myRoot);
		}
		if (myWindow != null) {
			myWindow.show();
			setupNavigation();
		}
	}

	@Override
	protected void hide_() {
		if (myWindow != null) {
			myWindow.hide();
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	protected void update() {
		if (!myIsInProgress && myWindow != null) {
			setupNavigation();
		}
	}

	private void createPanel(FBReader activity, RelativeLayout root) {
		if (myWindow != null && activity == myWindow.getContext()) {
			return;
		}

		activity.getLayoutInflater().inflate(R.layout.navigation_panel, root);
		myWindow = (NavigationWindow)root.findViewById(R.id.navigation_panel);

		final SeekBar slider = (SeekBar)myWindow.findViewById(R.id.navigation_slider);
		final TextView text = (TextView)myWindow.findViewById(R.id.navigation_text);

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
				myIsInProgress = true;
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				myIsInProgress = false;
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

		final Button btnOk = (Button)myWindow.findViewById(R.id.navigation_ok);
		final Button btnCancel = (Button)myWindow.findViewById(R.id.navigation_cancel);
		View.OnClickListener listener = new View.OnClickListener() {
			public void onClick(View v) {
				final ZLTextWordCursor position = myStartPosition;
				if (v == btnCancel && position != null) {
					myFBReader.getTextView().gotoPosition(position);
				} else if (v == btnOk) {
					if (myStartPosition != null &&
						!myStartPosition.equals(myFBReader.getTextView().getStartCursor())) {
						myFBReader.addInvisibleBookmark(myStartPosition);
						myFBReader.storePosition();
					}
				}
				myStartPosition = null;
				Application.hideActivePopup();
				myFBReader.getViewWidget().reset();
				myFBReader.getViewWidget().repaint();
			}
		};
		btnOk.setOnClickListener(listener);
		btnCancel.setOnClickListener(listener);

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		btnOk.setText(buttonResource.getResource("ok").getValue());
		btnCancel.setText(buttonResource.getResource("cancel").getValue());
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

	final void removeWindow(Activity activity) {
		if (myWindow != null && activity == myWindow.getContext()) {
			final ViewGroup root = (ViewGroup)myWindow.getParent();
			myWindow.hide();
			root.removeView(myWindow);
			myWindow = null;
		}
	}
}
