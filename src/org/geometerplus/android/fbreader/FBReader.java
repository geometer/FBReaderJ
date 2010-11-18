/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

import java.util.LinkedList;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivity;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.library.Library;

public final class FBReader extends ZLAndroidActivity {
	final static int REPAINT_CODE = 1;

	static FBReader Instance;

	private int myFullScreenFlag;

	private static class NavigationButtonPanel extends ControlButtonPanel {
		public volatile boolean NavigateDragging;
		public ZLTextPosition StartPosition;

		@Override
		public void onShow() {
			if (Instance != null && myControlPanel != null) {
				Instance.setupNavigation(myControlPanel);
			}
		}

		@Override
		public void updateStates() {
			super.updateStates();
			if (!NavigateDragging && Instance != null && myControlPanel != null) {
				Instance.setupNavigation(myControlPanel);
			}
		}
	}

	private static class TextSearchButtonPanel extends ControlButtonPanel {
		@Override
		public void onHide() {
			final ZLTextView textView = (ZLTextView)ZLApplication.Instance().getCurrentView();
			textView.clearFindResults();
		}
	}

	private static TextSearchButtonPanel myTextSearchPanel;
	private static NavigationButtonPanel myNavigatePanel;

	@Override
	protected String fileNameForEmptyUri() {
		return Library.getHelpFile().getPath();
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Instance = this;
		final ZLAndroidApplication application = ZLAndroidApplication.Instance();
		myFullScreenFlag =
			application.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN, myFullScreenFlag
		);
		if (myTextSearchPanel == null) {
			myTextSearchPanel = new TextSearchButtonPanel();
			myTextSearchPanel.register();
		}
		if (myNavigatePanel == null) {
			myNavigatePanel = new NavigationButtonPanel();
			myNavigatePanel.register();
		}

		final FBReaderApp fbReader = (FBReaderApp)ZLApplication.Instance();
		fbReader.addAction(ActionCode.SHOW_LIBRARY, new ShowLibraryAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_PREFERENCES, new ShowPreferencesAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_BOOK_INFO, new ShowBookInfoAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_CONTENTS, new ShowTOCAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_BOOKMARKS, new ShowBookmarksAction(this, fbReader));
		fbReader.addAction(ActionCode.SHOW_NETWORK_LIBRARY, new ShowNetworkLibraryAction(this, fbReader));
		
		fbReader.addAction(ActionCode.SHOW_NAVIGATION, new ShowNavigationAction(this, fbReader));
		fbReader.addAction(ActionCode.SEARCH, new SearchAction(this, fbReader));
	}

	@Override
	public void onStart() {
		super.onStart();
		final ZLAndroidApplication application = ZLAndroidApplication.Instance();

		final int fullScreenFlag =
			application.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		if (fullScreenFlag != myFullScreenFlag) {
			finish();
			startActivity(new Intent(this, this.getClass()));
		}

		final RelativeLayout root = (RelativeLayout)findViewById(R.id.root_view);
		if (!myTextSearchPanel.hasControlPanel()) {
			final ControlPanel panel = new ControlPanel(this);

			panel.addButton(ActionCode.FIND_PREVIOUS, false, R.drawable.text_search_previous);
			panel.addButton(ActionCode.CLEAR_FIND_RESULTS, true, R.drawable.text_search_close);
			panel.addButton(ActionCode.FIND_NEXT, false, R.drawable.text_search_next);

			myTextSearchPanel.setControlPanel(panel, root, false);
		}
		if (!myNavigatePanel.hasControlPanel()) {
			final ControlPanel panel = new ControlPanel(this);
			final View layout = getLayoutInflater().inflate(R.layout.navigate, panel, false);
			createNavigation(layout);
			panel.setExtension(layout);
			myNavigatePanel.setControlPanel(panel, root, true);
		}

		findViewById(R.id.main_view).setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				if (!myNavigatePanel.getVisibility()) {
					navigate();
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		ControlButtonPanel.restoreVisibilities();
	}

	@Override
	public void onPause() {
		ControlButtonPanel.saveVisibilities();
		super.onPause();
	}

	@Override
	public void onStop() {
		ControlButtonPanel.removeControlPanels();
		super.onStop();
	}

	void showTextSearchControls(boolean show) {
		if (show) {
			myTextSearchPanel.show(true);
		} else {
			myTextSearchPanel.hide(false);
		}
	}

	protected ZLApplication createApplication(String fileName) {
		if (SQLiteBooksDatabase.Instance() == null) {
			new SQLiteBooksDatabase("READER");
		}
		return new FBReaderApp(fileName);
	}

	@Override
	public boolean onSearchRequested() {
		final LinkedList<Boolean> visibilities = new LinkedList<Boolean>();
		ControlButtonPanel.saveVisibilitiesTo(visibilities);
		ControlButtonPanel.hideAllPendingNotify();
		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(new SearchManager.OnCancelListener() {
			public void onCancel() {
				ControlButtonPanel.restoreVisibilitiesFrom(visibilities);
				manager.setOnCancelListener(null);
			}
		});
		final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
		startSearch(fbreader.TextSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REPAINT_CODE:
			{
				final FBReaderApp fbreader = (FBReaderApp)ZLApplication.Instance();
				fbreader.clearTextCaches();
				fbreader.repaintView();
				break;
			}
		}
	}

	public void navigate() {
		final ZLTextView textView = (ZLTextView)ZLApplication.Instance().getCurrentView();
		myNavigatePanel.NavigateDragging = false;
		myNavigatePanel.StartPosition = new ZLTextFixedPosition(textView.getStartCursor());
		myNavigatePanel.show(true);
	}

	private final void createNavigation(View layout) {
		final SeekBar slider = (SeekBar) layout.findViewById(R.id.book_position_slider);
		final TextView text = (TextView) layout.findViewById(R.id.book_position_text);

		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			private void gotoPage(int page) {
				final ZLView view = ZLApplication.Instance().getCurrentView();
				if (view instanceof ZLTextView) {
					ZLTextView textView = (ZLTextView) view;
					if (page == 1) {
						textView.gotoHome();
					} else {
						textView.gotoPage(page);
					}
					ZLApplication.Instance().repaintView();
				}
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				myNavigatePanel.NavigateDragging = false;
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				myNavigatePanel.NavigateDragging = true;
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

		final Button btnOk = (Button) layout.findViewById(android.R.id.button1);
		final Button btnCancel = (Button) layout.findViewById(android.R.id.button3);
		View.OnClickListener listener = new View.OnClickListener() {
			public void onClick(View v) {
				final ZLTextPosition position = myNavigatePanel.StartPosition;
				myNavigatePanel.StartPosition = null;
				if (v == btnCancel && position != null) {
					((ZLTextView) ZLApplication.Instance().getCurrentView()).gotoPosition(position);
				}
				myNavigatePanel.hide(true);
			}
		};
		btnOk.setOnClickListener(listener);
		btnCancel.setOnClickListener(listener);
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		btnOk.setText(buttonResource.getResource("ok").getValue());
		btnCancel.setText(buttonResource.getResource("cancel").getValue());
	}

	private final void setupNavigation(ControlPanel panel) {
		final SeekBar slider = (SeekBar) panel.findViewById(R.id.book_position_slider);
		final TextView text = (TextView) panel.findViewById(R.id.book_position_text);

		final ZLTextView textView = (ZLTextView) ZLApplication.Instance().getCurrentView();
		final int page = textView.computeCurrentPage();
		final int pagesNumber = textView.computePageNumber();

		if (slider.getMax() != (pagesNumber - 1)
				|| slider.getProgress() != (page - 1)) {
			slider.setMax(pagesNumber - 1);
			slider.setProgress(page - 1);
			text.setText(makeProgressText(page, pagesNumber));
		}
	}

	private static String makeProgressText(int page, int pagesNumber) {
		return "" + page + " / " + pagesNumber;
	}
}
